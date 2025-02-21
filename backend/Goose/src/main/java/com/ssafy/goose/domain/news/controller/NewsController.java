package com.ssafy.goose.domain.news.controller;

import com.ssafy.goose.domain.news.dto.NewsArticleDto;
import com.ssafy.goose.domain.news.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "News API", description = "뉴스 관련 API")
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @Operation(summary = "뉴스 업로드", description = "새로운 뉴스 기사를 업로드합니다.")
    @PostMapping("/upload")
    public ResponseEntity<NewsArticleDto> uploadNews(@RequestBody NewsArticleDto newsDto) {
        return ResponseEntity.ok(newsService.uploadNews(newsDto));
    }

    @Operation(summary = "뉴스 리스트 조회", description = "저장된 뉴스 기사를 전체 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<List<NewsArticleDto>> getNewsList() {
        return ResponseEntity.ok(newsService.getNewsList());
    }

    @Operation(summary = "뉴스 검색", description = "제목에 특정 키워드가 포함된 뉴스를 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<List<NewsArticleDto>> searchNews(
            @Parameter(description = "검색할 키워드") @RequestParam String keyword) {
        return ResponseEntity.ok(newsService.searchNews(keyword));
    }


    @Operation(summary = "뉴스 상세 조회", description = "newsId에 해당하는 뉴스를 조회합니다.")
    @GetMapping("/{newsId}")
    public ResponseEntity<NewsArticleDto> getNewsById(
            @Parameter(description = "조회할 뉴스 ID") @PathVariable String newsId) {
        Optional<NewsArticleDto> news = newsService.getNewsById(newsId);
        return news.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
