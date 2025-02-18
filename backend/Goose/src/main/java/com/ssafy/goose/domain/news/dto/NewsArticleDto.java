package com.ssafy.goose.domain.news.dto;

import com.ssafy.goose.domain.news.entity.NewsArticle;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsArticleDto {

    private String id;
    private String title;
    private String originalLink;
    private String naverLink;
    private String description;
    private String pubDate;
    private String content;
    private List<String> paragraphs;
    private List<Double> paragraphReliabilities;
    private List<String> paragraphReasons;
    private String topImage;
    private LocalDateTime extractedAt;
    private Double biasScore;
    private Double reliability;
    private Double aiRate;          // ✅ 추가: AI 종합 평가 점수
    private String newsAgency;      // ✅ 추가: 언론사 정보 필드
    private String reliabilityComment; // ✅ 추가: 신뢰도 평가 코멘트 (ex. "신뢰성 있는 기사입니다.")

    /**
     * 🔹 엔티티 → DTO 변환
     */
    public static NewsArticleDto fromEntity(NewsArticle article) {
        // extractedAt를 UTC로 가정하고, Asia/Seoul로 변환
        ZonedDateTime utcTime = article.getExtractedAt().atZone(ZoneId.of("UTC"));
        ZonedDateTime kstTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));

        return NewsArticleDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .originalLink(article.getOriginalLink())
                .naverLink(article.getNaverLink())
                .description(article.getDescription())
                .pubDate(article.getPubDate())
                .content(article.getContent())
                .paragraphs(article.getParagraphs())
                .topImage(article.getTopImage())
                .extractedAt(kstTime.toLocalDateTime())
                .biasScore(article.getBiasScore() != null ? article.getBiasScore() : 0.0)
                .reliability(article.getReliability() != null ? article.getReliability() : 50.0)
                .paragraphReliabilities(article.getParagraphReliabilities())
                .paragraphReasons(article.getParagraphReasons())
                .aiRate(article.getAiRate() != null ? article.getAiRate() : 0.0)
                .newsAgency(article.getNewsAgency())
                .reliabilityComment(generateReliabilityComment(
                        article.getReliability() != null ? article.getReliability() : 50.0,
                        article.getBiasScore() != null ? article.getBiasScore() : 0.0
                ))
                .build();
    }

    /**
     * 🔹 DTO → 엔티티 변환
     */
    public NewsArticle toEntity() {
        return NewsArticle.builder()
                .id(this.id)
                .title(this.title)
                .originalLink(this.originalLink)
                .naverLink(this.naverLink)
                .description(this.description)
                .pubDate(this.pubDate)
                .content(this.content)
                .paragraphs(this.paragraphs)
                .topImage(this.topImage)
                .extractedAt(this.extractedAt)
                .biasScore(this.biasScore != null ? this.biasScore : 0.0)
                .reliability(this.reliability != null ? this.reliability : 50.0)
                .paragraphReliabilities(this.paragraphReliabilities)
                .paragraphReasons(this.paragraphReasons)
                .aiRate(this.aiRate != null ? this.aiRate : 0.0)
                .newsAgency(this.newsAgency)
                .build();
    }

    /**
     * 🔹 날짜 변환 메서드 개선 (ISO 8601, RFC 1123 형식 지원)
     */
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

    /**
     * 🔹 신뢰도 및 편향성 점수 기반 코멘트 생성 메서드
     */
    public static String generateReliabilityComment(Double reliability, Double biasScore) {
        if (reliability == null) reliability = 50.0;
        if (biasScore == null) biasScore = 0.0;

        if (reliability > 70 && biasScore < 30) {
            return "해당 기사는 편향되지 않은 정보를 담고 있으며 과장된 내용이 포함되지 않은 신뢰성 있는 기사입니다.";
        } else if (reliability > 50 && biasScore < 50) {
            return "해당 기사는 비교적 신뢰할 수 있는 기사입니다.";
        } else if (reliability > 30 && biasScore < 70) {
            return "해당 기사는 일부 주장이 과장되었을 수 있습니다.";
        } else {
            return "해당 기사는 신뢰도가 낮고 편향적인 내용이 포함되어 있을 수 있으니 주의가 필요합니다.";
        }
    }
}
