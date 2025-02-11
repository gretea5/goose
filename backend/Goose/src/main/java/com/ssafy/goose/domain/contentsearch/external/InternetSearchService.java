package com.ssafy.goose.domain.contentsearch.external;

import com.ssafy.goose.domain.contentsearch.dto.NewsResponseDto;
import java.util.List;

public interface InternetSearchService {

    // 추출된 키워드 기반 뉴스 검색
    List<NewsResponseDto> search(String keyword);
}
