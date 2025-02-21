package com.ssafy.goose.domain.contentsearch.external;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter @Setter
@Document(collection = "reference_news")
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceNewsArticle {

    @Id
    private String id;       // MongoDB의 기본 ID
    private String title;    // 뉴스 기사 제목 필드
    private String content;  // 뉴스 기사 내용 필드
}
