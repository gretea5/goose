package com.ssafy.goose.domain.news.service;

import com.ssafy.goose.domain.contentsearch.external.NewsAgencyExtractor;
import com.ssafy.goose.domain.news.entity.NewsArticle;
import com.ssafy.goose.domain.news.entity.ReferenceNewsArticle;
import com.ssafy.goose.domain.news.service.airate.AiRateService;
import com.ssafy.goose.domain.news.service.bias.BiasAnalyseService;
import com.ssafy.goose.domain.news.service.bias.BiasAnalysisResult;
import com.ssafy.goose.domain.news.service.crawling.NewsContentScraping;
import com.ssafy.goose.domain.news.service.paragraph.NewsParagraphSplitService;
import com.ssafy.goose.domain.warning.entity.WarningNewsAgency;
import com.ssafy.goose.domain.warning.repository.WarningNewsAgencyRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class NewsAutoProcessingService {
    private final NewsContentScraping newsContentScraping;
    private final NewsParagraphSplitService newsParagraphSplitService;
    private final BiasAnalyseService biasAnalyseService;
    private final NewsStorageService newsStorageService;
    private final AiRateService aiRateService;
    private final WarningNewsAgencyRepository warningNewsAgencyRepository;
    private final NewsAgencyExtractor newsAgencyExtractor;

    private final EmbeddingStorageService embeddingStorageService;

    public NewsAutoProcessingService(NewsContentScraping newsContentScraping,
                                     NewsParagraphSplitService newsParagraphSplitService,
                                     BiasAnalyseService biasAnalyseService,
                                     NewsStorageService newsStorageService,
                                     AiRateService aiRateService,
                                     WarningNewsAgencyRepository warningNewsAgencyRepository,
                                     NewsAgencyExtractor newsAgencyExtractor,
                                     EmbeddingStorageService embeddingStorageService) {
        this.newsContentScraping = newsContentScraping;
        this.newsParagraphSplitService = newsParagraphSplitService;
        this.biasAnalyseService = biasAnalyseService;
        this.newsStorageService = newsStorageService;
        this.aiRateService = aiRateService;
        this.warningNewsAgencyRepository = warningNewsAgencyRepository;
        this.newsAgencyExtractor = newsAgencyExtractor;
        this.embeddingStorageService = embeddingStorageService;
    }

    @Transactional
    public void processAndStoreNewsArticles(Map<String, Object> newsData) {
        List<Map<String, Object>> newsItems = (List<Map<String, Object>>) newsData.get("items");

        for (Map<String, Object> item : newsItems) {
            String link = (String) item.get("link");
            if (link == null || link.isEmpty()) continue;

            Map<String, Object> scrapedData = newsContentScraping.extractArticle(link);
            if (scrapedData == null || !scrapedData.containsKey("text")) continue;

            String cleanTitle = (String) scrapedData.get("title");
            String content = (String) scrapedData.get("text");
            String topImage = (String) scrapedData.get("image");

            if (content.length() < 100) continue;

            List<String> paragraphs = newsParagraphSplitService.getSplitParagraphs(content);
            System.out.println("문단 분리 수행 완료, 문단 갯수 : " + paragraphs.size());

            String newsId = new ObjectId().toString();

            // ✅ 크로마DB 저장 (임베딩 저장) - 5번 분석 전에 실행
            embeddingStorageService.storeNews(
                    EmbeddingStorageService.EmbeddingRequest.builder()
                            .id(newsId)
                            .title(cleanTitle)
                            .content(content)
                            .paragraphs(paragraphs)
                            .pubDate((String) item.get("pubDate"))
                            .build()
            );
            System.out.println("News 임베딩 저장 완료: " + newsId);

            // 5. 편향성 분석 수행 (문단별 신뢰도/분석 사유 포함)
            BiasAnalysisResult analysisResult = biasAnalyseService.analyzeBias(newsId, cleanTitle, content, paragraphs);

            // 6. AI 확률 계산
            Double aiRate = aiRateService.calculateAiRate(cleanTitle, paragraphs);

            // 7. 언론사 신뢰도 가져오기
            String newsAgency = newsAgencyExtractor.extractNewsAgency(link);
            WarningNewsAgency agency = warningNewsAgencyRepository.findByNewsAgency(newsAgency);
            int ranking = (agency != null) ? agency.getRanking() : 999; // 없으면 최하위 취급

            // 8. 최종 신뢰도 계산
            Double resultReliability = calculateFinalReliability(analysisResult.getBiasScore(), aiRate, ranking);

            // 9. MongoDB 저장 객체 생성 (분석 결과 반영)
            NewsArticle article = NewsArticle.builder()
                    .id(newsId)
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
                    .reliability(resultReliability)
                    .paragraphReliabilities(analysisResult.getParagraphReliabilities())
                    .paragraphReasons(analysisResult.getParagraphReasons())
                    .aiRate(aiRate)
                    .newsAgency(newsAgency)
                    .build();

            // 10. MongoDB에 저장
            newsStorageService.saveToMongoDB(article);
        }
    }

    /**
     * 최종 신뢰도 계산 메서드
     * @param biasScore    편향성 점수 (0~100)
     * @param aiRate       AI 생성 확률 (0~1)
     * @param ranking      언론사 랭킹 (작을수록 신뢰 높음)
     * @return 최종 신뢰도 (0~1)
     */
    private Double calculateFinalReliability(Double biasScore, Double aiRate, int ranking) {
        double rankingNormalized = 1 - (ranking / 100.0);
        rankingNormalized = Math.max(0, Math.min(rankingNormalized, 1));

        double biasNormalized = biasScore / 100.0;

        return biasNormalized * 0.4 + aiRate * 0.3 + rankingNormalized * 0.3;
    }

    public void processAndStoreReferenceNewsArticles(Map<String, Object> newsData) {
        List<Map<String, Object>> newsItems = (List<Map<String, Object>>) newsData.get("items");

        for (Map<String, Object> item : newsItems) {
            String link = (String) item.get("link");
            if (link == null || link.isEmpty()) continue;

            Map<String, Object> scrapedData = newsContentScraping.extractArticle(link);
            if (scrapedData == null || !scrapedData.containsKey("text")) continue;

            String cleanTitle = (String) scrapedData.get("title");
            String content = (String) scrapedData.get("text");
            String topImage = (String) scrapedData.get("image");

            if (content.length() < 100) continue;

            List<String> paragraphs = newsParagraphSplitService.getSplitParagraphs(content);

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

            newsStorageService.saveReferenceToMongoDB(article);
        }
    }
}
