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
@Document(collection = "reference_news")  // ✅ 기존 news_articles 와 분리
public class ReferenceNewsArticle {

    @Id
    private String id;
    private String title;
    private String originalLink;
    private String naverLink;
    private String description;
    private String pubDate;
    private List<String> paragraphs;
    private String content;
    private String topImage;
    private LocalDateTime extractedAt;
}
