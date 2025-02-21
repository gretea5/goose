package com.ssafy.goose.domain.fakenews.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter @Setter
public class NewsStatisticsDto {

    private String correctAnswer;                     // 해당 뉴스 게임의 정답
    private String fakeReason;                        // 정답인 이유
    private Map<String, Double> selectionPercentages; // 선택지 별 사용자 선택 비율
}
