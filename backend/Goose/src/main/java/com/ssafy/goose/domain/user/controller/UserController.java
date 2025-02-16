package com.ssafy.goose.domain.user.controller;

import com.ssafy.goose.domain.user.dto.*;
import com.ssafy.goose.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User API", description = "회원가입, 로그인, 로그아웃 기능을 제공하는 API")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    public ResponseEntity<UserResponseDto> signup(@RequestBody SignupRequestDto signupRequest) {
        return ResponseEntity.ok(userService.signup(signupRequest));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰을 발급합니다.")
    public ResponseEntity<UserResponseDto> login(@RequestBody LoginRequestDto loginRequest) {
        return ResponseEntity.ok(userService.login(loginRequest));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 처리합니다.")
    public ResponseEntity<UserResponseDto> logout(@RequestBody RefreshTokenRequestDto refreshTokenRequest) {
        return ResponseEntity.ok(userService.logout(refreshTokenRequest.getRefreshToken()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "RefreshToken을 사용해 새로운 AccessToken을 발급합니다.")
    public ResponseEntity<UserResponseDto> refresh(@RequestBody RefreshTokenRequestDto refreshTokenRequest) {
        return ResponseEntity.ok(userService.refreshAccessToken(refreshTokenRequest.getRefreshToken()));
    }

    @GetMapping("/check-username")
    @Operation(summary = "아이디 중복 확인", description = "사용 가능한 아이디인지 확인합니다.")
    public ResponseEntity<UserCheckResponseDto> checkUsername(@RequestParam String username) {
        return ResponseEntity.ok(userService.checkUsernameAvailability(username));
    }

    @GetMapping("/check-nickname")
    @Operation(summary = "닉네임 중복 확인", description = "사용 가능한 닉네임인지 확인합니다.")
    public ResponseEntity<UserCheckResponseDto> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(userService.checkNicknameAvailability(nickname));
    }
}
