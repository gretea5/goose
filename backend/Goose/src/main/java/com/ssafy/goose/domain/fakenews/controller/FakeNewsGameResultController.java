package com.ssafy.goose.domain.fakenews.controller;

import com.ssafy.goose.domain.fakenews.dto.FakeNewsGameResultResponseDto;
import com.ssafy.goose.domain.fakenews.entity.FakeNewsGameResult;
import com.ssafy.goose.domain.fakenews.entity.FakeNews;
import com.ssafy.goose.domain.fakenews.dto.FakeNewsGameResultRequestDto;
import com.ssafy.goose.domain.fakenews.repository.mongo.FakeNewsRepository;
import com.ssafy.goose.domain.fakenews.service.FakeNewsGameResultService;
import com.ssafy.goose.domain.fakenews.service.FakeNewsService;
import com.ssafy.goose.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(name = "Game Result API", description = "게임 결과 저장 및 제출 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/game-result")
public class FakeNewsGameResultController {

    private final FakeNewsGameResultService gameResultService;
    private final FakeNewsRepository fakeNewsRepository;
    private final FakeNewsService fakeNewsService;
    private final JwtTokenProvider jwtTokenProvider;

    public FakeNewsGameResultController(FakeNewsGameResultService gameResultService,
                                        FakeNewsRepository fakeNewsRepository,
                                        FakeNewsService fakeNewsService,
                                        JwtTokenProvider jwtTokenProvider) {
        this.gameResultService = gameResultService;
        this.fakeNewsRepository = fakeNewsRepository;
        this.fakeNewsService = fakeNewsService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // JWT를 이용한 토큰 검증 및 username 추출 (로그인 / 비로그인 구분)
    private String extractUsername(String authHeader) {
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

    // JWT 토큰에서 nickname 추출
    private String extractNickname(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return "guest";
        }
        if (authHeader.startsWith("Bearer ")) {
            authHeader = authHeader.substring(7);
        }
        if (!jwtTokenProvider.validateToken(authHeader)) {
            return "guest";
        }
        return jwtTokenProvider.getNickname(authHeader);
    }

    @Operation(summary = "게임 결과 제출 및 통계 업데이트",
            description = "JSON 형식의 요청 본문을 통해 사용자가 게임을 풀고 선택한 결과를 제출하면, MongoDB의 정답과 비교하여 정답 여부를 판단한 후 MySQL에 결과를 저장합니다.")
    @PostMapping("/submit")
    public ResponseEntity<FakeNewsGameResultResponseDto> submitGameResult(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody FakeNewsGameResultRequestDto requestDto) {

        String username = extractUsername(authHeader);
        String nickname = extractNickname(authHeader);
        String newsId = requestDto.getNewsId();

        // MongoDB에서 해당 뉴스의 정답을 조회하여 비교
        Optional<FakeNews> newsOpt = fakeNewsRepository.findById(newsId);
        if (!newsOpt.isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        FakeNews news = newsOpt.get();
        Boolean correct = requestDto.getUserChoice().equals(news.getCorrectAnswer());

        // 통계 업데이트: 로그인 사용자(guest가 아닌)인 경우에만 업데이트
        if (!"guest".equals(username)) {
            fakeNewsService.updateNewsStatistics(newsId, requestDto.getUserChoice(), requestDto.getDwellTime(), nickname);
        }

        // 게임 결과를 MySQL에 저장 (guest 사용자도 저장하지만 통계 업데이트는 하지 않음)
        FakeNewsGameResult savedResult = gameResultService.saveGameResult(username, newsId, requestDto.getUserChoice(),
                correct, requestDto.getDwellTime());

        // 응답 DTO 매핑: 여기서 nickname은 JWT 토큰에서 추출한 nickname 사용
        FakeNewsGameResultResponseDto responseDto = new FakeNewsGameResultResponseDto();
        responseDto.setId(savedResult.getId());
        responseDto.setNickname(nickname);
        responseDto.setNewsId(savedResult.getNewsId());
        responseDto.setUserChoice(savedResult.getUserChoice());
        responseDto.setCorrect(savedResult.getCorrect());
        responseDto.setCorrectAnswer(news.getCorrectAnswer());
        responseDto.setDwellTime(savedResult.getDwellTime());
        responseDto.setSolvedAt(savedResult.getSolvedAt());

        return ResponseEntity.ok(responseDto);
    }
}
