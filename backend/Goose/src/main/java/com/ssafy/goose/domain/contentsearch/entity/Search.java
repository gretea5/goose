package com.ssafy.goose.domain.contentsearch.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Search {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;         // 고유 ID
    private String keywords; // BERT 모델로 추출된 키워드
    private String result;   // 검색 결과 (JSON 저장)
}
