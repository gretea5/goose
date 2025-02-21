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
@Document(collection = "user_private_news")
public class News {

    @Id
    private String id;                           // MongoDB 자동 생성 ID
    private String title;                        // 제목
    private String originalLink;                 // 원본 링크
    private String naverLink;                    // 네이버 링크
    private String description;                  // 요약
    private String pubDate;                      // 게시 날짜
    private List<String> paragraphs;             // 뉴스 본문에 포함된 주요 문단들
    private String content;                      // 본문 내용
    private String topImage;                     // 대표 이미지 URL
    private String newsAgency;                   // 언론사
    private LocalDateTime extractedAt;           // 추출 시간

    // 추가 필드: MongoDB 엔티티에서 편향성 및 신뢰도 분석 결과를 보관
    private Double biasScore;                    // 편향성 점수
    private Double reliability;                  // 기사 신뢰도 점수
    private List<Double> paragraphReliabilities; // 문단별 신뢰도 점수
    private List<String> paragraphReasons;       // 문단별 신뢰도 분석 결과(사유)
}
