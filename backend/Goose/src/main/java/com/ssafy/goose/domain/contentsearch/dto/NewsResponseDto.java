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
    private String newsAgency;
    private Double aiRate; // AI 평가 점수 필드 추가

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
                ", aiRate=" + aiRate +
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

    public String getEvaluationMessage() {
        if (biasScore != null && reliability != null) {
            if (biasScore < 40 && reliability > 70) {
                return "해당 기사는 편향되지 않은 정보를 담고 있으며 과장된 내용이 포함되지 않은 신뢰성 있는 기사입니다.";
            } else if (biasScore >= 40 && biasScore < 60 && reliability > 50) {
                return "해당 기사는 중립적이나 일부 편향된 내용이 포함될 수 있습니다.";
            } else if (biasScore >= 60 || reliability < 50) {
                return "해당 기사는 다소 편향적인 내용이 포함되어 있으며 신뢰성에 주의가 필요합니다.";
            } else {
                return "해당 기사의 신뢰성 및 편향성에 대한 평가가 명확하지 않습니다.";
            }
        } else {
            return "기사 분석 데이터가 부족하여 평가를 제공할 수 없습니다.";
        }
    }
}
