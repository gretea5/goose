package com.ssafy.goose.domain.contentsearch.external;

import com.ssafy.goose.domain.contentsearch.dto.NewsResponseDto;
import com.ssafy.goose.domain.news.entity.ReferenceNewsArticle;
import com.ssafy.goose.domain.news.repository.ReferenceNewsCustomRepository;
import com.ssafy.goose.domain.news.service.EmbeddingStorageService;
import com.ssafy.goose.domain.news.service.bias.BiasAnalyseService;
import com.ssafy.goose.domain.news.service.bias.BiasAnalysisResult;
import com.ssafy.goose.domain.news.service.crawling.NewsContentScraping;
import com.ssafy.goose.domain.news.service.paragraph.NewsParagraphSplitService;
import jakarta.annotation.PostConstruct;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class NewsSearchService implements InternetSearchService {

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    private static final String NAVER_NEWS_URL = "https://openapi.naver.com/v1/search/news.json?query=";

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private BiasAnalyseService biasAnalyseService;

    @Autowired
    private NewsContentScraping newsContentScraping;

    @Autowired
    private NewsParagraphSplitService newsParagraphSplitService;

    @Autowired
    private EmbeddingStorageService embeddingStorageService;

    @Autowired
    private NaverNewsFetcher naverNewsFetcher;

    @Autowired
    private NewsAgencyExtractor newsAgencyExtractor;

    @Autowired
    private ReferenceNewsCustomRepository referenceNewsCustomRepository;

    @PostConstruct
    public void init() {
        trustAllCertificates();
    }

    @Override
    public List<NewsResponseDto> search(String[] keywords) {
        long startTime = System.currentTimeMillis(); // ⏱️ 시작 시간 측정
        // 반환할 뉴스 개수 설정
        int resultCount = 5;

        // 1️⃣ MongoDB 텍스트 인덱스 검색
        Query query = new Query();
        String searchQuery = String.join(" ", keywords);
        query.addCriteria(Criteria.where("$text").is(new org.bson.Document("$search", searchQuery)));
        query.with(Sort.by(Sort.Order.desc("score")));
        query.limit(resultCount);

        // ✅ MongoDB 실행
        List<NewsResponseDto> mongoData = mongoTemplate.find(query, NewsResponseDto.class, "news_articles");

        int mongoDataSize = mongoData.size();
        int neededFromNaver = resultCount - mongoDataSize;
//        int neededFromNaver = 5;
//        int mongoDataSize = 0;

        // 2️⃣ MongoDB 데이터 부족 시 Naver API 호출
        List<NewsResponseDto> resultData = new ArrayList<>(mongoData);
//        if (mongoDataSize < resultCount) {
//            List<NewsResponseDto> naverData = naverNewsFetcher.fetchNaverNews(keywords);
//            resultData.addAll(naverData.subList(0, Math.min(neededFromNaver, naverData.size())));
//        }

        // ✅ 최대 resultCount(5개) 제한
        if (resultData.size() > resultCount) {
            resultData = resultData.subList(0, resultCount);
        }

        // 3️⃣ 레퍼런스 뉴스 탐색 (MongoDB에서 키워드 기반 검색)
        List<ReferenceNewsArticle> referenceNewsList = referenceNewsCustomRepository.findNewsByKeywords(keywords);

        // 4️⃣ 병렬 처리로 크로마DB 저장 및 신뢰도 분석 수행
        ExecutorService executor = Executors.newFixedThreadPool(30);

        List<CompletableFuture<NewsResponseDto>> futures = resultData.stream()
                .map(dto -> CompletableFuture.supplyAsync(() -> {
                    String newsId = new ObjectId().toString();
                    dto.setId(newsId);

                    // ✅ 크로마DB 저장 (임베딩 저장)
                    embeddingStorageService.storeNews(
                            EmbeddingStorageService.EmbeddingRequest.builder()
                                    .id(newsId)
                                    .title(dto.getTitle())
                                    .content(dto.getContent())
                                    .paragraphs(dto.getParagraphs())
                                    .pubDate(dto.getPubDate())
                                    .build()
                    );
                    System.out.println("News 임베딩 저장 완료: " + newsId);

                    // ✅ 신뢰도 분석 수행 (레퍼런스 뉴스와 비교)
                    BiasAnalysisResult analysisResult = biasAnalyseService.analyzeBiasWithReference(
                            dto.getId(),
                            dto.getTitle(),
                            dto.getContent(),
                            dto.getParagraphs(),
                            referenceNewsList
                    );

                    dto.setBiasScore(analysisResult.getBiasScore());
                    dto.setReliability(analysisResult.getReliability());
                    dto.setParagraphReliabilities(analysisResult.getParagraphReliabilities());
                    dto.setParagraphReasons(analysisResult.getParagraphReasons());

                    return dto;
                }, executor))
                .toList();

        // 5️⃣ 모든 병렬 작업 완료 대기
        List<NewsResponseDto> processedData = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        // ✅ 스레드풀 종료
        executor.shutdown();

        // ⏱️ 종료 시간 및 수행 시간 출력
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("🕒 search() 실행 시간: " + duration + "ms");

        return processedData;
    }

    @Override
    public NewsResponseDto searchByUrl(String url) {
        try {
            // 1️⃣ 뉴스 본문 크롤링 (타이틀만 사용)
            Map<String, Object> scrapedData = newsContentScraping.extractArticle(url);
            if (scrapedData == null || !scrapedData.containsKey("title")) {
                System.out.println("❌ 뉴스 본문 크롤링 실패");
                return null;
            }
            String cleanTitle = (String) scrapedData.get("title");

            // 2️⃣ MongoDB 텍스트 인덱스를 활용해 제목 기반 가장 유사한 뉴스 찾기
            Query query = new Query();
            query.addCriteria(Criteria.where("$text").is(new org.bson.Document("$search", cleanTitle)));
            query.with(Sort.by(Sort.Order.desc("score")));
            query.limit(1);

            NewsResponseDto similarNewsDto = mongoTemplate.findOne(query, NewsResponseDto.class, "news_articles");

            if (similarNewsDto == null) {
                System.out.println("❌ 유사 뉴스 제목 기반 MongoDB 조회 실패");
                return null;
            }

            // ✅ 유사한 뉴스 기사를 그대로 반환
            return similarNewsDto;

        } catch (Exception e) {
            System.err.println("❌ searchByUrl() 실패: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }




    // ✅ SSL 인증 우회 설정
    private static void trustAllCertificates() {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }}, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
