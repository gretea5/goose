package com.ssafy.goose.domain.user.service;

import com.ssafy.goose.domain.news.repository.NewsRepository;
import com.ssafy.goose.domain.user.dto.*;
import com.ssafy.goose.domain.user.entity.User;
import com.ssafy.goose.domain.user.repository.UserRepository;
import com.ssafy.goose.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final NewsRepository newsRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       NewsRepository newsRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.newsRepository = newsRepository;
    }

    // 회원가입
    public UserResponseDto signup(SignupRequestDto signupRequest) {
        if (userRepository.findByUsername(signupRequest.getUsername()).isPresent()) {
            return UserResponseDto.error("이미 존재하는 username 입니다.");
        }
        if (userRepository.findByNickname(signupRequest.getNickname()).isPresent()) {
            return UserResponseDto.error("이미 존재하는 nickname 입니다.");
        }

        User user = User.builder()
                .username(signupRequest.getUsername())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .nickname(signupRequest.getNickname())
                .createdAt((int) Instant.now().getEpochSecond())
                .isDeleted(false)
                .token(null)
                .build();

        userRepository.save(user);
        // 회원가입 성공 시, 토큰 없이 닉네임만 반환합니다.
        return UserResponseDto.success(user.getNickname());
    }

    // 로그인
    public UserResponseDto login(LoginRequestDto loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElse(null);

        if (user == null) {
            return UserResponseDto.error("존재하지 않는 사용자입니다.");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return UserResponseDto.error("비밀번호가 일치하지 않습니다.");
        }

        return generateNewTokens(user); // ✅ 로그인 시점에서 동일한 토큰 처리 로직 사용
    }

    // 로그아웃
    public UserResponseDto logout(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return UserResponseDto.error("유효하지 않은 RefreshToken입니다.");
        }

        String username = jwtTokenProvider.getUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // RefreshToken 삭제 (DB에서 무효화)
        user.setToken(null);
        userRepository.save(user);

        // 로그아웃 성공 시, 닉네임을 반환합니다.
        return UserResponseDto.success(user.getNickname());
    }

    // RefreshToken을 사용한 AccessToken 재발급
    public UserResponseDto refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            // 검증 실패 시 DB에서 삭제하고 에러 반환
            User user = userRepository.findByToken(refreshToken).orElse(null);
            if (user != null) {
                user.setToken(null);
                userRepository.save(user);
            }
            return UserResponseDto.error("유효하지 않은 RefreshToken입니다.");
        }

        String username = jwtTokenProvider.getUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return generateNewTokens(user);
    }

    // 회원정보 수정
    public UserResponseDto updateUser(UpdateUserRequestDto updateRequest, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        boolean updated = false;

        // newNickname 수정 (중복 체크)
        if (updateRequest.getNewNickname() != null && !updateRequest.getNewNickname().trim().isEmpty()
                && !updateRequest.getNewNickname().equals(user.getNickname())) {
            if (userRepository.findByNickname(updateRequest.getNewNickname()).isPresent()) {
                return UserResponseDto.error("이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(updateRequest.getNewNickname());
            updated = true;
        }

        // newPassword 수정 (암호화 후 저장)
        if (updateRequest.getNewPassword() != null && !updateRequest.getNewPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateRequest.getNewPassword()));
            updated = true;
        }
        userRepository.save(user);

        if (updated) {
            return generateNewTokens(user);
        } else {
            return UserResponseDto.success(user.getNickname());
        }
    }

    // AccessToken & RefreshToken을 생성하는 공통 메서드
    private UserResponseDto generateNewTokens(User user) {
        // createAccessToken() 메서드는 이제 username과 nickname을 모두 받아 토큰에 클레임으로 저장합니다.
        String accessToken = jwtTokenProvider.createAccessToken(user.getUsername(), user.getNickname());

        // 기존 RefreshToken 유지, 없으면 새로 발급
        String refreshToken = user.getToken();
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            refreshToken = jwtTokenProvider.createRefreshToken(user.getUsername());
            user.setToken(refreshToken);
            userRepository.save(user);
        }

        return UserResponseDto.success(accessToken, refreshToken, user.getNickname());
    }

    // 회원 가입 시 ID 중복 체크
    public UserCheckResponseDto checkUsernameAvailability(String username) {
        boolean exists = userRepository.findByUsername(username).isPresent();
        return exists ? new UserCheckResponseDto(false, "이미 사용 중인 ID입니다.")
                : new UserCheckResponseDto(true, "사용 가능한 ID입니다.");
    }

    // 회원 가입 시 닉네임 중복 체크
    public UserCheckResponseDto checkNicknameAvailability(String nickname) {
        boolean exists = userRepository.findByNickname(nickname).isPresent();
        return exists ? new UserCheckResponseDto(false, "이미 사용 중인 닉네임입니다.")
                : new UserCheckResponseDto(true, "사용 가능한 닉네임입니다.");
    }

    public String extractUsername(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return "guest";
        }
        if (authHeader.startsWith("Bearer ")) {
            authHeader = authHeader.substring(7);
        }
        if (!jwtTokenProvider.validateToken(authHeader)) {
            return "guest";
        }
        return jwtTokenProvider.getUsername(authHeader);
    }
}
