package com.ssafy.goose.domain.fakenews.controller;

import com.ssafy.goose.domain.fakenews.dto.*;
import com.ssafy.goose.domain.fakenews.entity.FakeNews;
import com.ssafy.goose.domain.fakenews.entity.FakeNewsGameResult;
import com.ssafy.goose.domain.fakenews.repository.mongo.FakeNewsRepository;
import com.ssafy.goose.domain.fakenews.repository.jpa.FakeNewsGameResultRepository;
import com.ssafy.goose.domain.user.entity.User;
import com.ssafy.goose.domain.user.repository.UserRepository;
import com.ssafy.goose.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
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
    private final UserRepository userRepository;

    public FakeNewsStatisticsController(FakeNewsGameResultRepository gameResultRepository,
                                        FakeNewsRepository fakeNewsRepository,
                                        JwtTokenProvider jwtTokenProvider,
                                        UserRepository userRepository) {
        this.gameResultRepository = gameResultRepository;
        this.fakeNewsRepository = fakeNewsRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
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

    @Operation(summary = "사용자 게임 결과 통계 조회",
            description = "로그인 사용자는 선택한 옵션, 정답 여부, 체류시간, 본인 닉네임 및 문제 풀이 시각과 함께 해당 뉴스의 체류시간 Top 3(닉네임+체류시간)를 반환합니다. 비로그인 사용자는 닉네임과 풀이 시각 없이 반환합니다.")
    @GetMapping("/game-result")
    public ResponseEntity<UserGameResultDto> getUserGameResult(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam String newsId) {
        String username = extractUsername(authHeader);
        String nickname = extractNickname(authHeader);

        // 해당 사용자의 게임 결과 조회 (MySQL)
        List<FakeNewsGameResult> results = gameResultRepository.findByUsername(username)
                .stream().filter(r -> r.getNewsId().equals(newsId))
                .collect(Collectors.toList());
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
        List<UserGameResultDto.RankingDTO> rankingDTOs = null;
        if (newsOpt.isPresent()) {
            FakeNews news = newsOpt.get();
            rankingDTOs = news.getDwellTimeRanking().stream()
                    .map(r -> {
                        UserGameResultDto.RankingDTO dto = new UserGameResultDto.RankingDTO();
                        dto.setNickname(r.getNickname());
                        dto.setDwellTime(r.getDwellTime());
                        return dto;
                    }).collect(Collectors.toList());
        }

        UserGameResultDto dto = new UserGameResultDto();
        dto.setUserChoice(latest.getUserChoice());
        dto.setCorrect(latest.getCorrect());
        dto.setDwellTime(latest.getDwellTime());
        dto.setNickname(nickname);
        dto.setSolvedAt(latest.getSolvedAt());
        dto.setRanking(rankingDTOs);

        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "뉴스 게임 전체 통계 조회",
            description = "특정 뉴스의 정답, 정답인 이유, 그리고 선택지별 사용자 선택 비율을 MongoDB에서 조회하여 반환합니다.")
    @GetMapping("/news")
    public ResponseEntity<NewsStatisticsDto> getNewsStatistics(@RequestParam String newsId) {
        Optional<FakeNews> newsOpt = fakeNewsRepository.findById(newsId);
        if (!newsOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        FakeNews news = newsOpt.get();
        NewsStatisticsDto dto = new NewsStatisticsDto();
        dto.setCorrectAnswer(news.getCorrectAnswer());
        dto.setFakeReason(news.getFakeReason());
        dto.setSelectionPercentages(news.getSelectionPercentages());
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "마이페이지 통계 조회",
            description = "로그인 사용자의 경우, 닉네임, 지금까지 푼 총 문제 수, 맞춘 정답 수, 그리고 각 게임의 풀이 시각 목록 및 정답률(%)을 반환합니다.")
    @GetMapping("/mypage")
    public ResponseEntity<MyPageStatisticsDto> getMyPageStatistics(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = extractUsername(authHeader);
        String nickname = extractNickname(authHeader);
        if ("guest".equals(nickname)) {
            return ResponseEntity.status(401).build();
        }
        List<FakeNewsGameResult> results = gameResultRepository.findByUsername(username);
        if (results.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        // 회원가입 정보를 얻기 위해 User 엔티티 조회
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).build();
        }
        LocalDateTime registrationDate = LocalDateTime.ofEpochSecond(user.getCreatedAt(), 0, ZoneOffset.ofHours(9));

        // 회원가입 이전의 게임 기록은 제외
        List<FakeNewsGameResult> filteredResults = results.stream()
                .filter(r -> !r.getSolvedAt().isBefore(registrationDate))
                .collect(Collectors.toList());

        // 전체 통계 계산 (전체 게임 수, 맞춘 게임 수, 전체 정답률)
        int totalQuestions = filteredResults.size();
        int correctCount = (int) filteredResults.stream().filter(FakeNewsGameResult::getCorrect).count();
        double overallRate = totalQuestions > 0 ? ((double) correctCount / totalQuestions) * 100 : 0;
        overallRate = Math.floor(overallRate); // 소수점 없이 정수 처리

        // 게임 기록 그룹화
        Map<YearMonth, List<FakeNewsGameResult>> grouped = filteredResults.stream()
                .collect(Collectors.groupingBy(r -> YearMonth.from(r.getSolvedAt())));

        // 회원가입 월부터 현재 월까지의 목록 생성
        YearMonth currentMonth = YearMonth.from(LocalDateTime.now());
        YearMonth regMonth = YearMonth.from(registrationDate);
        List<YearMonth> monthList = new ArrayList<>();
        YearMonth temp = regMonth;
        while (!temp.isAfter(currentMonth)) {
            monthList.add(temp);
            temp = temp.plusMonths(1);
        }
        // 최근 최대 6개월만 반환 (만약 회원가입 이후 기간이 6개월 미만이면 그 만큼의 기간만)
        if (monthList.size() > 6) {
            monthList = monthList.subList(monthList.size() - 6, monthList.size());
        }

        // 월별 정답률 계산: 각 월에 대한 총 게임 수, 맞춘 게임 수, 정답률 계산
        List<MyPageStatisticsDto.MonthlyStatDto> monthlyStats = new ArrayList<>();
        for (YearMonth ym : monthList) {
            List<FakeNewsGameResult> monthResults = grouped.getOrDefault(ym, new ArrayList<>());
            int mTotal = monthResults.size();
            int mCorrect = (int) monthResults.stream().filter(FakeNewsGameResult::getCorrect).count();
            int mRate = mTotal > 0 ? (int) ((double) mCorrect / mTotal * 100) : 0;
            MyPageStatisticsDto.MonthlyStatDto stat = new MyPageStatisticsDto.MonthlyStatDto();
            stat.setMonth(ym.getMonthValue());
            stat.setCorrectRate(mRate);
            monthlyStats.add(stat);
        }

        MyPageStatisticsDto dto = new MyPageStatisticsDto();
        dto.setNickname(nickname);
        dto.setTotalQuestions(totalQuestions);
        dto.setCorrectCount(correctCount);
        dto.setCorrectRate(overallRate);
        dto.setGameRecords(monthlyStats);

        return ResponseEntity.ok(dto);
    }
}
