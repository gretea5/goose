package com.ssafy.goose.domain.game.statistics.service;

import com.ssafy.goose.domain.game.statistics.dto.QuestionStatisticsResponseDto;
import com.ssafy.goose.domain.game.statistics.dto.RankingResponseDto;
import com.ssafy.goose.domain.game.statistics.entity.QuestionStatistics;
import com.ssafy.goose.domain.game.statistics.entity.SolvingTimeRanking;
import com.ssafy.goose.domain.game.statistics.repository.QuestionStatisticsRepository;
import com.ssafy.goose.domain.game.statistics.repository.SolvingTimeRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameStatisticsService {
    private final QuestionStatisticsRepository questionStatisticsRepository;
    private final SolvingTimeRankingRepository solvingTimeRankingRepository;

    public QuestionStatisticsResponseDto getQuestionStatistics(Long questionId) {
        QuestionStatistics statistics = questionStatisticsRepository.findByQuestionId(questionId);
        if (statistics == null) {
            statistics = QuestionStatistics.builder()
                    .questionId(questionId)
                    .build();
        }

        return QuestionStatisticsResponseDto.builder()
                .option1Percentage(statistics.getOption1Percentage())
                .option2Percentage(statistics.getOption2Percentage())
                .option3Percentage(statistics.getOption3Percentage())
                .option4Percentage(statistics.getOption4Percentage())
                .totalAttempts(statistics.getTotalAttempts())
                .build();
    }

    @Transactional
    public void recordAnswer(Long questionId, int selectedOption) {
        QuestionStatistics statistics = questionStatisticsRepository.findByQuestionId(questionId);
        if (statistics == null) {
            statistics = QuestionStatistics.builder()
                    .questionId(questionId)
                    .build();
        }
        statistics.incrementOptionCount(selectedOption);
        questionStatisticsRepository.save(statistics);
    }

    @Transactional
    public void recordSolvingTime(Long userId, String nickname, Long questionId, Duration solvingTime) {
        SolvingTimeRanking ranking = SolvingTimeRanking.builder()
                .userId(userId)
                .nickname(nickname)
                .questionId(questionId)
                .solvingTime(solvingTime)
                .build();
        solvingTimeRankingRepository.save(ranking);
    }

    public RankingResponseDto getRankingInfo(Long questionId, Long userId) {
        List<SolvingTimeRanking> top3Rankings = solvingTimeRankingRepository.findTop3ByQuestionId(questionId);
        Long userRank = solvingTimeRankingRepository.getUserRankForQuestion(userId, questionId);

        return RankingResponseDto.builder()
                .top3Rankings(top3Rankings.stream()
                        .map(RankingResponseDto.RankingDetailDto::from)
                        .toList())
                .userRank(userRank)
                .build();
    }
}
