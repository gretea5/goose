package com.ssafy.goose.domain.fakenews.controller;

import com.ssafy.goose.domain.fakenews.entity.FakeNewsGameResult;
import com.ssafy.goose.domain.fakenews.entity.FakeNews;
import com.ssafy.goose.domain.fakenews.dto.FakeNewsGameResultRequestDto;
import com.ssafy.goose.domain.fakenews.repository.mongo.FakeNewsRepository;
import com.ssafy.goose.domain.fakenews.service.FakeNewsGameResultService;
import com.ssafy.goose.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Game Result API", description = "게임 결과 저장 및 제출 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/game-result")
public class FakeNewsGameResultController {

    private final FakeNewsGameResultService gameResultService;
    private final FakeNewsRepository fakeNewsRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public FakeNewsGameResultController(FakeNewsGameResultService gameResultService,
                                        FakeNewsRepository fakeNewsRepository,
                                        JwtTokenProvider jwtTokenProvider) {
        this.gameResultService = gameResultService;
        this.fakeNewsRepository = fakeNewsRepository;
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

    @Operation(summary = "게임 결과 제출",
            description = "JSON 형식의 요청 본문을 통해 사용자가 게임을 풀고 선택한 결과를 제출하면, MongoDB의 정답과 비교하여 정답 여부를 판단한 후 MySQL에 결과를 저장합니다.")
    @PostMapping("/submit")
    public ResponseEntity<FakeNewsGameResult> submitGameResult(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody FakeNewsGameResultRequestDto requestDto) {

        String username = extractUsername(authHeader);

        // MongoDB에서 해당 뉴스의 정답을 조회하여 비교
        FakeNews news = fakeNewsRepository.findById(requestDto.getNewsId()).orElse(null);
        if (news == null) {
            return ResponseEntity.badRequest().build();
        }
        Boolean correct = requestDto.getUserChoice().equals(news.getCorrectAnswer());

        FakeNewsGameResult result = gameResultService.saveGameResult(username, requestDto.getNewsId(), requestDto.getUserChoice(),
                correct, requestDto.getDwellTime(), requestDto.getTotalQuestions(), requestDto.getCorrectCount());
        return ResponseEntity.ok(result);
    }
}
