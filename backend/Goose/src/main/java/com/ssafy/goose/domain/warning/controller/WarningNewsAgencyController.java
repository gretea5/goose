package com.ssafy.goose.domain.warning.controller;

import com.ssafy.goose.domain.warning.service.NewsWarningCrawlerService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warnings")
public class WarningNewsAgencyController {

    private final NewsWarningCrawlerService newsWarningCrawlerService;

    public WarningNewsAgencyController(NewsWarningCrawlerService newsWarningCrawlerService) {
        this.newsWarningCrawlerService = newsWarningCrawlerService;
    }

    @PostMapping("/crawl")
    @Operation(summary = "정정보도 크롤링 실행", description = "네이버 뉴스 정정보도 페이지를 크롤링하여 언론사별 경고 기사 개수를 저장합니다.")
    public String triggerCrawling() {
        newsWarningCrawlerService.crawlAndSaveNewsWarnings();
        return "크롤링 및 데이터 저장 완료";
    }
}
