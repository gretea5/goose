package com.ssafy.goose.domain.fakenews.controller;

import com.ssafy.goose.domain.fakenews.dto.MyPageStatisticsDTO;
import com.ssafy.goose.domain.fakenews.dto.NewsStatisticsDTO;
import com.ssafy.goose.domain.fakenews.dto.UserGameResultDTO;
import com.ssafy.goose.domain.fakenews.entity.FakeNews;
import com.ssafy.goose.domain.fakenews.entity.FakeNewsGameResult;
import com.ssafy.goose.domain.fakenews.repository.mongo.FakeNewsRepository;
import com.ssafy.goose.domain.fakenews.repository.jpa.FakeNewsGameResultRepository;
import com.ssafy.goose.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Game Statistics API", description = "게임 결과 통계 및 마이페이지 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/statistics")
public class FakeNewsStatisticsController {

    private final FakeNewsGameResultRepository gameResultRepository;
    private final FakeNewsRepository fakeNewsRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public FakeNewsStatisticsController(FakeNewsGameResultRepository gameResultRepository,
                                        FakeNewsRepository fakeNewsRepository,
                                        JwtTokenProvider jwtTokenProvider) {
        this.gameResultRepository = gameResultRepository;
        this.fakeNewsRepository = fakeNewsRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // JWT 토큰 검증 및 username 추출
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

    @Operation(summary = "사용자 게임 결과 통계 조회",
            description = "로그인 사용자는 선택한 옵션, 정답 여부, 체류시간, 본인 닉네임 및 문제 풀이 시각과 함께 해당 뉴스의 체류시간 Top 3(닉네임+체류시간)를 반환합니다. 비로그인 사용자는 닉네임과 풀이 시각 없이 반환합니다.")
    @GetMapping("/game-result")
    public ResponseEntity<UserGameResultDTO> getUserGameResult(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = extractUsername(authHeader);

        // 해당 사용자의 게임 결과 조회 (MySQL)
        List<FakeNewsGameResult> results = gameResultRepository.findByUsername(username);
        if (results.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        // 최신순 결과 (풀이 시각 기준 내림차순)
        FakeNewsGameResult latest = results.stream()
                .max(Comparator.comparing(FakeNewsGameResult::getSolvedAt))
                .orElse(null);
        if (latest == null) {
            return ResponseEntity.noContent().build();
        }

        // 해당 게임 뉴스의 체류시간 랭킹 정보 (MongoDB)
        Optional<FakeNews> newsOpt = fakeNewsRepository.findById(latest.getNewsId());
        List<UserGameResultDTO.RankingDTO> rankingDTOs = null;
        if (newsOpt.isPresent()) {
            FakeNews news = newsOpt.get();
            rankingDTOs = news.getDwellTimeRanking().stream()
                    .map(r -> {
                        UserGameResultDTO.RankingDTO dto = new UserGameResultDTO.RankingDTO();
                        dto.setNickname(r.getNickname());
                        dto.setDwellTime(r.getDwellTime());
                        return dto;
                    }).collect(Collectors.toList());
        }

        UserGameResultDTO dto = new UserGameResultDTO();
        dto.setUserChoice(latest.getUserChoice());
        dto.setCorrect(latest.getCorrect());
        dto.setDwellTime(latest.getDwellTime());

        if ("guest".equals(username)) {
            dto.setNickname("guest");
            dto.setSolvedAt(null);
        } else {
            dto.setNickname(username);
            dto.setSolvedAt(latest.getSolvedAt());
        }
        dto.setRanking(rankingDTOs);

        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "뉴스 게임 전체 통계 조회",
            description = "특정 뉴스의 정답, 정답인 이유, 그리고 선택지별 사용자 선택 비율을 MongoDB에서 조회하여 반환합니다.")
    @GetMapping("/news")
    public ResponseEntity<NewsStatisticsDTO> getNewsStatistics(@RequestParam String newsId) {
        Optional<FakeNews> newsOpt = fakeNewsRepository.findById(newsId);
        if (!newsOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        FakeNews news = newsOpt.get();
        NewsStatisticsDTO dto = new NewsStatisticsDTO();
        dto.setCorrectAnswer(news.getCorrectAnswer());
        dto.setFakeReason(news.getFakeReason());
        dto.setSelectionPercentages(news.getSelectionPercentages());
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "마이페이지 통계 조회",
            description = "로그인 사용자의 경우 username, 지금까지 푼 총 문제 수, 맞춘 정답 수, 그리고 각 게임의 풀이 시각 목록 및 정답률(%)을 반환합니다.")
    @GetMapping("/mypage")
    public ResponseEntity<MyPageStatisticsDTO> getMyPageStatistics(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = extractUsername(authHeader);
        if ("guest".equals(username)) {
            // 비로그인 사용자는 마이페이지 통계 제공 불가
            return ResponseEntity.status(401).build();
        }
        List<FakeNewsGameResult> results = gameResultRepository.findByUsername(username);
        if (results.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        int totalQuestions = results.size();
        int correctCount = (int) results.stream().filter(FakeNewsGameResult::getCorrect).count();
        double correctRate = totalQuestions > 0 ? ((double) correctCount / totalQuestions) * 100 : 0;
        List<MyPageStatisticsDTO.GameRecordDTO> records = results.stream().map(r -> {
            MyPageStatisticsDTO.GameRecordDTO rec = new MyPageStatisticsDTO.GameRecordDTO();
            rec.setSolvedAt(r.getSolvedAt());
            return rec;
        }).collect(Collectors.toList());

        MyPageStatisticsDTO dto = new MyPageStatisticsDTO();
        dto.setUsername(username);
        dto.setTotalQuestions(totalQuestions);
        dto.setCorrectCount(correctCount);
        dto.setCorrectRate(correctRate);
        dto.setGameRecords(records);
        return ResponseEntity.ok(dto);
    }
}
