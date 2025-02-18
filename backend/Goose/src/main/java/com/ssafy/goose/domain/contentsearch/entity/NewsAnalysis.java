package com.ssafy.goose.domain.contentsearch.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "news_analysis")
public class NewsAnalysis {

    @Id
    private String id;                           // MongoDB 자동 생성 ID

    // 기존 뉴스 분석 결과 데이터
    private String title;
    private String originalLink;
    private String naverLink;
    private String description;
    private String pubDate;
    private List<String> paragraphs;
    private String content;
    private String topImage;
    private String newsAgency;
    private LocalDateTime extractedAt;
    private Double biasScore;
    private Double reliability;
    private List<Double> paragraphReliabilities;
    private List<String> paragraphReasons;
    private Double aiRate;
    private String evaluationMessage;            // AI 평가 문구

    // 추가 필드: 분석 요청 시각, 분석 종류, 사용자 로그인 ID
    private LocalDateTime analysisRequestedAt;   // 분석 요청 시각 (타임스탬프)
    private String analysisType;                 // 분석 종류 ("image", "content", "url")
    private String username;                     // 분석을 요청한 사용자의 로그인 ID
}
