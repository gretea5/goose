package com.ssafy.goose.domain.contentsearch.controller;

import com.ssafy.goose.domain.contentsearch.dto.KeywordRequestDto;
import com.ssafy.goose.domain.contentsearch.dto.KeywordResponseDto;
import com.ssafy.goose.domain.contentsearch.dto.NewsResponseDto;
import com.ssafy.goose.domain.contentsearch.external.NewsSearchService;
import com.ssafy.goose.domain.contentsearch.service.ContentService;
import com.ssafy.goose.domain.contentsearch.service.KeywordService;
import com.ssafy.goose.domain.contentsearch.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Content Search API", description = "키워드 추출 및 검색 API")
@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentSearchController {

    private final KeywordService keywordService;
    private final SearchService searchService;
    private final ContentService contentService;
    private final NewsSearchService newsSearchService;

    // 키워드 추출 API
    @PostMapping("/keywords")
    @Operation(summary = "키워드 추출", description = "OCR로 추출된 텍스트를 받아 키워드를 추출합니다.")
    public KeywordResponseDto extractKeywords(@RequestBody KeywordRequestDto requestDto) {
        // OCR 텍스트를 받아 키워드를 추출하고, 그 결과를 응답으로 반환
        return keywordService.extractKeywords(requestDto.getText());
    }

    // 키워드로 뉴스 검색 API
    @GetMapping("/search")
    @Operation(summary = "키워드 검색", description = "키워드를 기반으로 DB 또는 인터넷에서 검색 결과를 반환합니다.")
    public List<NewsResponseDto> searchKeywords(@RequestParam String keyword) {
        // 여러 키워드를 처리하기 위해 쉼표로 구분된 키워드를 배열로 변환
        String[] keywords = keyword.split(",");
        return searchService.searchNewsByKeyword(keywords);
    }

    // 안드로이드에서 텍스트 받아서 처리하는 API
    @PostMapping("/keywords-search")
    @Operation(summary = "안드로이드 텍스트 처리", description = "안드로이드에서 OCR로 변환된 텍스트를 받아 키워드 추출 및 검색을 수행합니다.")
    public List<NewsResponseDto> extractKeywordsAndSearch(@RequestBody KeywordRequestDto requestDto) {
        // 안드로이드에서 받은 텍스트로 키워드 추출 및 뉴스 검색 실행
        return contentService.processKeywordAndSearch(requestDto.getText());
    }

    // URL로 뉴스 본문 가져오기 및 분석 API
    @GetMapping("/search-by-url")
    @Operation(summary = "뉴스 URL 검색", description = "뉴스 URL을 입력받아 본문 크롤링, 문단 분리, 신뢰도 분석을 수행합니다.")
    public NewsResponseDto searchNewsByUrl(@RequestParam String url) {
        return newsSearchService.searchByUrl(url);
    }
}
