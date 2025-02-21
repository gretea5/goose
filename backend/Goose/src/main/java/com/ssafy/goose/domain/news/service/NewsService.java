package com.ssafy.goose.domain.news.service;

import com.ssafy.goose.domain.news.dto.NewsArticleDto;
import com.ssafy.goose.domain.news.entity.NewsArticle;
import com.ssafy.goose.domain.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;

    // 뉴스 업로드
    @Transactional
    public NewsArticleDto uploadNews(NewsArticleDto newsDto) {
        NewsArticle article = NewsArticle.builder()
                .title(newsDto.getTitle())
                .originalLink(newsDto.getOriginalLink())
                .naverLink(newsDto.getNaverLink())
                .description(newsDto.getDescription())
                .pubDate(newsDto.getPubDate())
                .content(newsDto.getContent())
                .topImage(newsDto.getTopImage())
                .extractedAt(newsDto.getExtractedAt())
                .build();

        NewsArticle savedArticle = newsRepository.save(article);
        return NewsArticleDto.fromEntity(savedArticle);
    }

    // 🔹 최신순으로 상위 10개 뉴스 리스트 조회
    public List<NewsArticleDto> getNewsList() {
        return newsRepository.findTop10ByOrderByExtractedAtDesc()
                .stream()
                .map(NewsArticleDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 뉴스 검색 (제목 기반)
    public List<NewsArticleDto> searchNews(String keyword) {
        String regexPattern = ".*" + keyword + ".*";  // MongoDB 정규식 패턴 적용
        return newsRepository.findByTitleRegex(regexPattern)
                .stream()
                .map(article -> {
                    NewsArticleDto dto = NewsArticleDto.fromEntity(article);
                    dto.getPubDateTimestamp();
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 뉴스 상세 조회
    public Optional<NewsArticleDto> getNewsById(String newsId) {
        return newsRepository.findById(newsId)
                .map(article -> {
                    NewsArticleDto dto = NewsArticleDto.fromEntity(article);
                    dto.getPubDateTimestamp();
                    return dto;
                });
    }
}
