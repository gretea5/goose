package com.ssafy.goose.domain.contentsearch.external;

import com.ssafy.goose.domain.contentsearch.dto.NewsResponseDto;
import java.util.List;

public interface InternetSearchService {

    // 키워드 기반 뉴스 검색
    List<NewsResponseDto> search(String[] keywords);

    NewsResponseDto searchByUrl(String url);

    // 언론사 정보 가져오기 (크롤링)
    String extractNewsAgency(String newsUrl);
}
