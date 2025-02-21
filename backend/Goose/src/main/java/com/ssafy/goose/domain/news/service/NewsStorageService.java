package com.ssafy.goose.domain.news.service;

import com.ssafy.goose.domain.news.service.bias.BiasAnalyseService;
import com.ssafy.goose.domain.news.service.crawling.NewsContentScraping;
import com.ssafy.goose.domain.news.service.paragraph.NewsParagraphSplitService;
import com.ssafy.goose.domain.news.entity.NewsArticle;
import com.ssafy.goose.domain.news.entity.ReferenceNewsArticle;
import com.ssafy.goose.domain.news.repository.NewsRepository;
import com.ssafy.goose.domain.news.repository.ReferenceNewsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NewsStorageService {
    private final NewsRepository newsRepository;
    private final ReferenceNewsRepository referenceNewsRepository;

    public NewsStorageService(NewsRepository newsRepository, ReferenceNewsRepository referenceNewsRepository) {
        this.newsRepository = newsRepository;
        this.referenceNewsRepository = referenceNewsRepository;
    }

    public void saveToMongoDB(NewsArticle article) {
        newsRepository.save(article);
    }

    public void saveReferenceToMongoDB(ReferenceNewsArticle article) {
        referenceNewsRepository.save(article);
    }
}

