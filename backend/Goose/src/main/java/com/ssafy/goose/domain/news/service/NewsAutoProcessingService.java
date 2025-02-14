package com.ssafy.goose.domain.news.service;

import com.ssafy.goose.domain.news.entity.NewsArticle;
import com.ssafy.goose.domain.news.entity.ReferenceNewsArticle;
import com.ssafy.goose.domain.news.service.bias.BiasAnalyseService;
import com.ssafy.goose.domain.news.service.bias.BiasAnalysisResult;
import com.ssafy.goose.domain.news.service.crawling.NewsContentScraping;
import com.ssafy.goose.domain.news.service.paragraph.NewsParagraphSplitService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class NewsAutoProcessingService {
    private final NewsContentScraping newsContentScraping;
    private final NewsParagraphSplitService newsParagraphSplitService;
    private final BiasAnalyseService biasAnalyseService;
    private final NewsStorageService newsStorageService;

    public NewsAutoProcessingService(NewsContentScraping newsContentScraping,
                                     NewsParagraphSplitService newsParagraphSplitService,
                                     BiasAnalyseService biasAnalyseService,
                                     NewsStorageService newsStorageService) {
        this.newsContentScraping = newsContentScraping;
        this.newsParagraphSplitService = newsParagraphSplitService;
        this.biasAnalyseService = biasAnalyseService;
        this.newsStorageService = newsStorageService;
    }

    /**
     * 뉴스 기사를 크롤링, 분석, 가공한 후 MongoDB에 저장하는 메서드
     */
    public void processAndStoreNewsArticles(Map<String, Object> newsData) {
        List<Map<String, Object>> newsItems = (List<Map<String, Object>>) newsData.get("items");

        for (Map<String, Object> item : newsItems) {
            // 1. 뉴스 기사 링크 가져오기
            String link = (String) item.get("link");
            if (link == null || link.isEmpty()) continue;

            // 2. FastAPI를 이용해 뉴스 본문과 대표 이미지 크롤링
            Map<String, Object> scrapedData = newsContentScraping.extractArticle(link);
            if (scrapedData == null || !scrapedData.containsKey("text")) continue;

            String cleanTitle = (String) scrapedData.get("title");
            String content = (String) scrapedData.get("text");
            String topImage = (String) scrapedData.get("image");

            // 3. 본문이 너무 짧은 경우 제외
            if (content.length() < 100) continue;

            // 4. 문단 분리 수행 (FastAPI 이용)
            List<String> paragraphs = newsParagraphSplitService.getSplitParagraphs(content);

            System.out.println("문단 분리 수행 완료, 문단 갯수 : " + paragraphs.size());

            // 5. 편향성 분석 수행 (문단별 신뢰도/분석 사유 포함)
            BiasAnalysisResult analysisResult = biasAnalyseService.analyzeBiasMyNews(cleanTitle, content, paragraphs);

            // 6. MongoDB 저장 객체 생성 (분석 결과 반영)
            NewsArticle article = NewsArticle.builder()
                    .title(cleanTitle)
                    .originalLink((String) item.get("originallink"))
                    .naverLink(link)
                    .description((String) item.get("description"))
                    .pubDate((String) item.get("pubDate"))
                    .content(content)
                    .paragraphs(paragraphs)
                    .topImage(topImage)
                    .extractedAt(LocalDateTime.now())
                    .biasScore(analysisResult.getBiasScore())
                    .reliability(analysisResult.getReliability())
                    .paragraphReliabilities(analysisResult.getParagraphReliabilities())
                    .paragraphReasons(analysisResult.getParagraphReasons())
                    .build();

            // 7. MongoDB에 저장
            newsStorageService.saveToMongoDB(article);
        }
    }

    /**
     * 참고용 뉴스 기사를 크롤링, 분석, 가공한 후 MongoDB에 저장하는 메서드
     */
    public void processAndStoreReferenceNewsArticles(Map<String, Object> newsData) {
        List<Map<String, Object>> newsItems = (List<Map<String, Object>>) newsData.get("items");

        for (Map<String, Object> item : newsItems) {
            // 1. 뉴스 기사 링크 가져오기
            String link = (String) item.get("link");
            if (link == null || link.isEmpty()) continue;

            // 2. FastAPI를 이용해 뉴스 본문과 대표 이미지 크롤링
            Map<String, Object> scrapedData = newsContentScraping.extractArticle(link);
            if (scrapedData == null || !scrapedData.containsKey("text")) continue;

            String cleanTitle = (String) scrapedData.get("title");
            String content = (String) scrapedData.get("text");
            String topImage = (String) scrapedData.get("image");

            // 3. 본문이 너무 짧은 경우 제외
            if (content.length() < 100) continue;

            // 4. 문단 분리 수행 (FastAPI 이용)
            List<String> paragraphs = newsParagraphSplitService.getSplitParagraphs(content);

            // 5. MongoDB 저장 객체 생성
            ReferenceNewsArticle article = ReferenceNewsArticle.builder()
                    .title(cleanTitle)
                    .originalLink((String) item.get("originallink"))
                    .naverLink(link)
                    .description((String) item.get("description"))
                    .pubDate((String) item.get("pubDate"))
                    .content(content)
                    .paragraphs(paragraphs)
                    .topImage(topImage)
                    .extractedAt(LocalDateTime.now())
                    .build();

            // 6. MongoDB에 저장
            newsStorageService.saveReferenceToMongoDB(article);
        }
    }
}
