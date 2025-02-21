package com.ssafy.goose.domain.contentsearch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class NewsRequestDto {

    private String keyword; // 뉴스 검색에 사용될 키워드
}
