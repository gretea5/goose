package com.ssafy.goose.domain.contentsearch.controller;

import com.ssafy.goose.domain.contentsearch.dto.KeywordRequestDto;
import com.ssafy.goose.domain.contentsearch.dto.KeywordResponseDto;
import com.ssafy.goose.domain.contentsearch.dto.NewsResponseDto;
import com.ssafy.goose.domain.contentsearch.entity.NewsAnalysis;
import com.ssafy.goose.domain.contentsearch.external.NewsSearchService;
import com.ssafy.goose.domain.contentsearch.service.ContentService;
import com.ssafy.goose.domain.contentsearch.service.KeywordService;
import com.ssafy.goose.domain.contentsearch.service.NewsAnalysisService;
import com.ssafy.goose.domain.contentsearch.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
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
    private final NewsAnalysisService newsAnalysisService;

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
    @Operation(summary = "안드로이드 텍스트 처리", description = "안드로이드에서 텍스트를 받아 키워드 추출 및 검색을 수행합니다.")
    public List<NewsResponseDto> extractKeywordsAndSearch(@RequestBody KeywordRequestDto requestDto) {
        // 1. 안드로이드에서 받은 텍스트로 키워드 추출 및 뉴스 검색 실행
        List<NewsResponseDto> result = contentService.processKeywordAndSearch(requestDto.getText());

        // 2. 로그인 여부 확인 (AccessToken 유효 여부)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();

            // 3. 로그인 된 사용자만 뉴스 분석 결과 MongoDB에 저장
            for (NewsResponseDto news : result) {
                NewsAnalysis analysis = NewsAnalysis.builder()
                        .title(news.getTitle())
                        .originalLink(news.getOriginalLink())
                        .naverLink(news.getNaverLink())
                        .description(news.getDescription())
                        .pubDate(news.getPubDate())
                        .paragraphs(news.getParagraphs())
                        .content(news.getContent())
                        .topImage(news.getTopImage())
                        .newsAgency(news.getNewsAgency())
                        .extractedAt(news.getExtractedAt() != null ? news.getExtractedAt() : LocalDateTime.now())
                        .biasScore(news.getBiasScore())
                        .reliability(news.getReliability())
                        .paragraphReliabilities(news.getParagraphReliabilities())
                        .paragraphReasons(news.getParagraphReasons())
                        .analysisRequestedAt(LocalDateTime.now())
                        .analysisType(requestDto.getAnalysisType())
                        .username(username)
                        .build();
                newsAnalysisService.saveAnalysis(analysis);
            }
        }

        return result;
    }

    // 사용자별 분석 기록 조회 API 추가
    @GetMapping("/my-news-analysis")
    @Operation(summary = "사용자별 뉴스 분석 기록 조회", description = "로그인한 사용자의 뉴스 분석 기록을 조회합니다.")
    public List<NewsAnalysis> getMyNewsAnalysis() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            return newsAnalysisService.getAnalysesByUsername(username);
        } else {
            // 로그인하지 않은 경우 401 Unauthorized 응답
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
    }

    // URL로 뉴스 본문 가져오기 및 분석 API
    @GetMapping("/search-by-url")
    @Operation(summary = "뉴스 URL 검색", description = "뉴스 URL을 입력받아 본문 크롤링, 문단 분리, 신뢰도 분석을 수행합니다.")
    public NewsResponseDto searchNewsByUrl(@RequestParam String url) {
        return newsSearchService.searchByUrl(url);
    }
}
