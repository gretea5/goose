package com.ssafy.goose.domain.contentsearch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsResponseDto {

    private String id;                           // MongoDB 자동 생성 ID
    private String title;                        // 제목
    private String originalLink;                 // 원본 링크
    private String naverLink;                    // 네이버 링크
    private String description;                  // 요약
    private String pubDate;                      // 게시 날짜
    private String content;                      // 본문 내용
    private List<String> paragraphs;             // 뉴스 본문에 포함된 주요 문단들
    private List<Double> paragraphReliabilities; // 문단별 신뢰도 점수
    private List<String> paragraphReasons;       // 문단별 신뢰도 분석 결과(사유)
    private String topImage;                     // 대표 이미지 URL
    private LocalDateTime extractedAt;           // 추출 시간
    private Double biasScore;                    // 편향성 점수
    private Double reliability;                  // 기사 신뢰도 점수
    private String newsAgency;                   // 언론사 정보 추가

    @Override
    public String toString() {
        return "NewsResponseDto{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", originalLink='" + originalLink + '\'' +
                ", naverLink='" + naverLink + '\'' +
                ", description='" + description + '\'' +
                ", pubDate='" + pubDate + '\'' +
                ", content='" + content + '\'' +
                ", paragraphs=" + paragraphs +
                ", paragraphReliabilities=" + paragraphReliabilities +
                ", paragraphReasons=" + paragraphReasons +
                ", topImage='" + topImage + '\'' +
                ", extractedAt=" + extractedAt +
                ", biasScore=" + biasScore +
                ", reliability=" + reliability +
                ", newsAgency='" + newsAgency + '\'' +
                '}';
    }


    public long getPubDateTimestamp() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                    "EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH
            );
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(this.pubDate, formatter);
            return zonedDateTime.toInstant().toEpochMilli();
        } catch (Exception e) {
            try {
                return ZonedDateTime.parse(this.pubDate).toInstant().toEpochMilli();
            } catch (Exception ex) {
                return -1;
            }
        }
    }
}
