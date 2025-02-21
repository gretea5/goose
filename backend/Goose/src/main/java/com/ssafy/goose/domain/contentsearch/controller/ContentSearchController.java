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

    @PostMapping("/keywords")
    @Operation(summary = "키워드 추출", description = "OCR로 추출된 텍스트를 받아 키워드를 추출합니다.")
    public KeywordResponseDto extractKeywords(@RequestBody KeywordRequestDto requestDto) {
        return keywordService.extractKeywords(requestDto.getText());
    }

    @GetMapping("/search")
    @Operation(summary = "키워드 검색", description = "키워드를 기반으로 DB 또는 인터넷에서 검색 결과를 반환합니다.")
    public List<NewsResponseDto> searchKeywords(@RequestParam String keyword) {
        String[] keywords = keyword.split(",");
        return searchService.searchNewsByKeyword(keywords);
    }

    @PostMapping("/keywords-search")
    @Operation(summary = "안드로이드 텍스트 처리", description = "안드로이드에서 텍스트를 받아 키워드 추출 및 검색을 수행합니다.")
    public List<NewsResponseDto> extractKeywordsAndSearch(@RequestBody KeywordRequestDto requestDto) {
        List<NewsResponseDto> result = contentService.processKeywordAndSearch(requestDto.getText());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();

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
                        .aiRate(news.getAiRate())
                        .evaluationMessage(news.getEvaluationMessage())
                        .analysisRequestedAt(LocalDateTime.now())
                        .analysisType(requestDto.getAnalysisType())
                        .username(username)
                        .build();
                newsAnalysisService.saveAnalysis(analysis);
            }
        }

        return result;
    }

    @GetMapping("/my-news-analysis")
    @Operation(summary = "사용자별 뉴스 분석 기록 조회", description = "로그인한 사용자의 뉴스 분석 기록을 조회합니다.")
    public List<NewsAnalysis> getMyNewsAnalysis() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            return newsAnalysisService.getAnalysesByUsername(username);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
    }

    @GetMapping("/search-by-url")
    @Operation(summary = "뉴스 URL 검색", description = "뉴스 URL을 입력받아 본문 크롤링, 문단 분리, 신뢰도 분석을 수행합니다.")
    public NewsResponseDto searchNewsByUrl(@RequestParam String url) {
        return newsSearchService.searchByUrl(url);
    }
}
