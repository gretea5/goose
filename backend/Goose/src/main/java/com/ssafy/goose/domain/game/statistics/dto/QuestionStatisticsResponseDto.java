package com.ssafy.goose.domain.game.statistics.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuestionStatisticsResponseDto {
    private double option1Percentage;
    private double option2Percentage;
    private double option3Percentage;
    private double option4Percentage;
    private int totalAttempts;
}
