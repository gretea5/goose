package com.ssafy.goose.domain.game.statistics.repository;

import com.ssafy.goose.domain.game.statistics.entity.QuestionStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionStatisticsRepository extends JpaRepository<QuestionStatistics, Long> {
    QuestionStatistics findByQuestionId(Long questionId);
}
