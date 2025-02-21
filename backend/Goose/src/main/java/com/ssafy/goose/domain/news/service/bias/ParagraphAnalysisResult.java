package com.ssafy.goose.domain.news.service.bias;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParagraphAnalysisResult {
    private List<Double> reliabilityScores;
    private List<String> bestMatches;

    /**
     * reliabilityScores의 평균값을 반환합니다.
     * 만약 값이 없으면 기본값 50.0을 반환합니다.
     */
    public double getAverageReliability() {
        return reliabilityScores.stream().mapToDouble(Double::doubleValue).average().orElse(50.0);
    }
}
