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
import java.time.LocalDateTime;
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
        // 반환할 갯수
        int resultCount = 5;

        // 1️⃣ MongoDB 텍스트 인덱스 검색
        Query query = new Query();

        // ✅ 여러 키워드를 하나의 문자열로 합쳐서 "금리 인상 OR 환율" 이런식으로 검색 (OR 검색)
        String searchQuery = String.join(" ", keywords);

        // ✅ $text 연산자 사용 -> 텍스트 인덱스 활용
        query.addCriteria(Criteria.where("$text").is(new org.bson.Document("$search", searchQuery)));

        // ✅ 점수 기준 정렬 (검색 연관도 높은 순서)
        query.with(Sort.by(Sort.Order.desc("score")));

        // ✅ 최대 5개 제한
        query.limit(resultCount);

        // ✅ MongoDB 실행
        List<NewsResponseDto> mongoData = mongoTemplate.find(query, NewsResponseDto.class, "news_articles");

        int mongoDataSize = mongoData.size();
        int neededFromNaver = resultCount - mongoDataSize;

        // 2️⃣ MongoDB 데이터 부족 시 Naver API 호출
        List<NewsResponseDto> resultData = new ArrayList<>(mongoData);
        if (mongoDataSize < resultCount) {
            List<NewsResponseDto> naverData = naverNewsFetcher.fetchNaverNews(keywords);
            resultData.addAll(naverData.subList(0, Math.min(neededFromNaver, naverData.size())));
        }

        // 최대 5개까지만 사용
        if (resultData.size() > resultCount) {
            resultData = resultData.subList(0, resultCount);
        }

        // 3.1. 레퍼런스 뉴스 탐색 및 크로마 DB 저장
        List<ReferenceNewsArticle> referenceNewsList = referenceNewsCustomRepository.findNewsByKeywords(keywords);


        // 3.2. 레퍼런스 뉴스 임베딩 저장 (비동기 병렬 처리)
        ExecutorService executor_reference = Executors.newFixedThreadPool(20); // 병렬 처리 스레드 수 설정

        List<CompletableFuture<Void>> futures_reference = referenceNewsList.stream()
                .map(referenceNews -> CompletableFuture.runAsync(() -> {
                    embeddingStorageService.storeReferenceNews(
                            EmbeddingStorageService.EmbeddingRequest.builder()
                                    .id(referenceNews.getId())
                                    .title(referenceNews.getTitle())
                                    .content(referenceNews.getContent())
                                    .paragraphs(referenceNews.getParagraphs())
                                    .pubDate(referenceNews.getPubDate())
                                    .build()
                    );
                    System.out.println("referenceNews 임베딩 저장 완료: " + referenceNews.getId());
                }, executor_reference))
                .toList();

        // 모든 작업 완료 대기
        CompletableFuture.allOf(futures_reference.toArray(new CompletableFuture[0])).join();

        executor_reference.shutdown();


        // 3️⃣ 병렬 처리로 크로마DB 저장 및 신뢰도 분석 수행
        ExecutorService executor = Executors.newFixedThreadPool(30);

        List<CompletableFuture<NewsResponseDto>> futures = resultData.stream()
                .map(dto -> CompletableFuture.supplyAsync(() -> {
                    String newsId = new ObjectId().toString();
                    dto.setId(newsId);

                    // 4️⃣ 크로마DB 저장
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

                    // 5️⃣ 신뢰도 분석 수행
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

        // 6️⃣ 모든 병렬 작업 완료 대기
        List<NewsResponseDto> processedData = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        // 7️⃣ 스레드풀 종료
        executor.shutdown();

        return processedData;
    }

    @Override
    public NewsResponseDto searchByUrl(String url) {
        try {
            // 1️⃣ 뉴스 본문 크롤링
            Map<String, Object> scrapedData = newsContentScraping.extractArticle(url);
            if (scrapedData == null || !scrapedData.containsKey("text")) {
                System.out.println("❌ 뉴스 본문 크롤링 실패");
                return null;
            }

            String cleanTitle = (String) scrapedData.get("title");
            String content = (String) scrapedData.get("text");
            String topImage = (String) scrapedData.get("image");

            if (content.length() < 100) {
                System.out.println("❌ 본문이 너무 짧아서 제외");
                return null;
            }

            // 2️⃣ 문단 분리 수행
            List<String> paragraphs = newsParagraphSplitService.getSplitParagraphs(content);

            // 3️⃣ ID 생성 및 기사 객체 생성
            String newsId = new ObjectId().toString();
            NewsResponseDto newsDto = NewsResponseDto.builder()
                    .id(newsId)
                    .title(cleanTitle)
                    .originalLink(url)
                    .naverLink(url)
                    .description("")  // URL 직접 검색이므로 description 없음
                    .pubDate("")       // URL 직접 검색이므로 pubDate 없음
                    .content(content)
                    .paragraphs(paragraphs)
                    .paragraphReliabilities(new ArrayList<>())
                    .paragraphReasons(new ArrayList<>())
                    .topImage(topImage)
                    .extractedAt(LocalDateTime.now())
                    .biasScore(0.0)
                    .reliability(50.0)
                    .build();

            // 4️⃣ 크로마 DB 저장 & 신뢰도 분석 병렬 실행
            ExecutorService executor = Executors.newFixedThreadPool(20);

            CompletableFuture<Void> embeddingFuture = CompletableFuture.runAsync(() ->
                    embeddingStorageService.storeNews(
                            EmbeddingStorageService.EmbeddingRequest.builder()
                                    .id(newsId)
                                    .title(cleanTitle)
                                    .content(content)
                                    .paragraphs(paragraphs)
                                    .pubDate("")
                                    .build()
                    ), executor);

            CompletableFuture<BiasAnalysisResult> analysisFuture = CompletableFuture.supplyAsync(() ->
                    biasAnalyseService.analyzeBias(
                            newsDto.getId(),
                            newsDto.getTitle(),
                            newsDto.getContent(),
                            newsDto.getParagraphs()
                    ), executor);

            // 5️⃣ 병렬 작업 완료 대기
            embeddingFuture.join(); // 임베딩 저장 완료 대기
            BiasAnalysisResult analysisResult = analysisFuture.join(); // 분석 완료 대기

            // 6️⃣ 분석 결과 반영
            newsDto.setBiasScore(analysisResult.getBiasScore());
            newsDto.setReliability(analysisResult.getReliability());
            newsDto.setParagraphReliabilities(analysisResult.getParagraphReliabilities());
            newsDto.setParagraphReasons(analysisResult.getParagraphReasons());

            executor.shutdown();

            return newsDto;

        } catch (Exception e) {
            System.err.println("❌ searchByUrl() 실패: " + e.getMessage());
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
