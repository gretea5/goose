package com.ssafy.goose.domain.contentsearch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;        // 고유 ID
    private String content; // 안드로이드에서 받아온 원본 텍스트
    private String keyword; // BERT 모델로 추출된 키워드

    @ElementCollection
    @CollectionTable(name = "content_results", joinColumns = @JoinColumn(name = "content_id"))
    @Column(name = "result")
    private List<String> result; // 검색 결과 리스트
}
