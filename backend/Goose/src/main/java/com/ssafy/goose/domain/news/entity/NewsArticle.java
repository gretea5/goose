package com.ssafy.goose.domain.news.entity;

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
@Document(collection = "news_articles")
public class NewsArticle {

    @Id
    private String id;                  // MongoDB 자동 생성 ID
    private String title;               // 뉴스 제목
    private String originalLink;        // 원본 뉴스 링크
    private String naverLink;           // 네이버 뉴스 링크
    private String description;         // 뉴스 설명
    private String pubDate;             // 뉴스 게시 날짜 (String 타입 유지)
    private List<String> paragraphs;    // 뉴스 문단 (전문으로부터 분리된 문단들)
    private List<Double> paragraphReliabilities; // 문단별 신뢰도 점수
    private List<String> paragraphReasons;       // 문단별 신뢰도 분석 결과(사유)
    private String content;             // 뉴스 본문 내용
    private String topImage;            // 뉴스 대표 이미지 URL
    private LocalDateTime extractedAt;  // 뉴스 크롤링 시점
    private Double biasScore;           // 편향성 점수
    private Double reliability;         // 기사 신뢰도 점수
    private Double aiRate;              // ✅ AI 평가 종합 점수
    private String newsAgency;          // ✅ 언론사 정보 추가 필드
}
