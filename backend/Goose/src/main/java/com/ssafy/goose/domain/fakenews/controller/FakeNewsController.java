package com.ssafy.goose.domain.fakenews.controller;

import com.ssafy.goose.domain.fakenews.entity.FakeNews;
import com.ssafy.goose.domain.fakenews.dto.FakeNewsResultRequestDto;
import com.ssafy.goose.domain.fakenews.service.FakeNewsService;
import com.ssafy.goose.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Fake News Game API", description = "가짜 뉴스 판별 게임 관련 API")
@RestController
@RequestMapping("/api/fake-news")
public class FakeNewsController {

    private final FakeNewsService fakeNewsService;
    private final JwtTokenProvider jwtTokenProvider;

    public FakeNewsController(FakeNewsService fakeNewsService, JwtTokenProvider jwtTokenProvider) {
        this.fakeNewsService = fakeNewsService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // JWT 토큰 검증 및 username 추출 (로그인 / 비로그인 구분)
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

    @Operation(summary = "랜덤 가짜 뉴스 반환",
            description = "게임 탭 접속 시, 로그인 여부에 따라 이미 풀었던 뉴스는 제외하고 MongoDB에서 랜덤으로 가짜 뉴스 하나를 반환합니다.")
    @GetMapping("/random")
    public ResponseEntity<FakeNews> getRandomNews(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = extractUsername(authHeader);
        FakeNews news = fakeNewsService.getRandomFakeNews(username);
        if (news == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(news);
    }

    @Operation(summary = "사용자 선택 결과 업데이트",
            description = "JSON 형식의 요청 본문을 통해 사용자가 선택한 옵션과 체류 시간을 받아 MongoDB의 통계(투표수, 선택 비율, 체류시간 랭킹)를 업데이트합니다.")
    @PostMapping("/result")
    public ResponseEntity<?> submitResult(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody FakeNewsResultRequestDto requestDto) {

        String username = extractUsername(authHeader);
        String nickname = (requestDto.getNickname() == null || requestDto.getNickname().trim().isEmpty())
                ? username : requestDto.getNickname();
        fakeNewsService.updateNewsStatistics(requestDto.getNewsId(), requestDto.getUserChoice(), requestDto.getDwellTime(), nickname);
        return ResponseEntity.ok().build();
    }
}
