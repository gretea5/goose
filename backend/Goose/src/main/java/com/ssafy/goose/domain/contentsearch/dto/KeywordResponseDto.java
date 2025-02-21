package com.ssafy.goose.domain.contentsearch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class KeywordResponseDto {

    private String[] keywords; // BERT 모델로 추출된 키워드 리스트
}
