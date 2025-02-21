package com.ssafy.goose.domain.news.service.bias;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BiasAnalysisResult {
    private double biasScore;                    // 최종 편향(신뢰) 점수
    private double reliability;                  // 최종 신뢰도 (여기서는 biasScore와 동일하게 처리)
    private List<Double> paragraphReliabilities; // 문단별 신뢰도 점수
    private List<String> paragraphReasons;       // 문단별 신뢰도 분석 사유 (또는 결과)
}
