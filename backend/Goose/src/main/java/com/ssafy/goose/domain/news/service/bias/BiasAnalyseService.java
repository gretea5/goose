package com.ssafy.goose.domain.news.service.bias;

import com.ssafy.goose.domain.news.entity.ReferenceNewsArticle;
import com.ssafy.goose.domain.news.repository.ReferenceNewsCustomRepository;
import com.ssafy.goose.domain.news.service.EmbeddingStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class BiasAnalyseService {

    private final ReferenceNewsCustomRepository referenceNewsCustomRepository;
    private final AnalyseByTitle analyseByTitle;
    private final AnalyseByContent analyseByContent;
    private final AnalyzeParagraph analyzeParagraph;
    private final EmbeddingStorageService embeddingStorageService;

    private final RestTemplate restTemplate = new RestTemplate(); // ✅ 추가됨

    // ✅ 병렬 처리 스레드풀 설정
    private final ExecutorService executorService = new ThreadPoolExecutor(
            5, 10, 30L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            new CustomizableThreadFactory("bias-analyse-")
    );

    public BiasAnalysisResult analyzeBias(String id, String title, String content, List<String> paragraphs) {
        System.out.println("analyzeBias 수행, title : " + title);

        List<ReferenceNewsArticle> referenceNewsList = referenceNewsCustomRepository.findNewsByKeywords(title, content);
        if (referenceNewsList.isEmpty()) {
            System.out.println("❌ 해당 키워드와 관련된 최근 뉴스 없음");
            return BiasAnalysisResult.builder()
                    .biasScore(50.0)
                    .reliability(50.0)
                    .paragraphReliabilities(null)
                    .paragraphReasons(null)
                    .build();
        }

        // ✅ 레퍼런스 뉴스 임베딩 저장 - 비동기 병렬 처리
        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<CompletableFuture<Void>> futures = referenceNewsList.stream()
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
                }, executor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // ✅ 임베딩 저장 후 분석 실행 (referenceNewsList 함께 전달)
        return analyzeBiasWithReference(id, title, content, paragraphs, referenceNewsList);
    }

    public BiasAnalysisResult analyzeBiasWithReference(
            String id, String title, String content, List<String> paragraphs, List<ReferenceNewsArticle> referenceNewsList) {

        System.out.println("analyzeBiasWithReference 수행 (본문 기반 유사도 검색), title : " + title);

        try {
            CompletableFuture<Double> titleFuture = CompletableFuture.supplyAsync(
                    () -> analyseByTitle.checkTitleWithReference(id, referenceNewsList), executorService
            );

            CompletableFuture<Double> contentFuture = CompletableFuture.supplyAsync(
                    () -> analyseByContent.checkContentWithReference(id, referenceNewsList), executorService
            );

            CompletableFuture<List<String>> similarReferenceIdsFuture = CompletableFuture.supplyAsync(() -> {
                // ✅ FastAPI 호출 → 본문(content) 임베딩 → 유사 reference_id 5개 가져오기
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, String> requestBody = Map.of("query", content, "n_results", "5");

                HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<Map> response = restTemplate.postForEntity(
                        "http://i12d208.p.ssafy.io/:5061/get-similar-references",
                        requestEntity,
                        Map.class
                );

                List<String> referenceIds = (List<String>) response.getBody().get("reference_ids");
                return referenceIds;
            }, executorService);

            List<String> referenceParagraphIds = similarReferenceIdsFuture.get();

            CompletableFuture<ParagraphAnalysisResult> paragraphFuture = CompletableFuture.supplyAsync(
                    () -> analyzeParagraph.analyze(title, paragraphs, referenceParagraphIds),
                    executorService
            );

            Double bias_title = titleFuture.get();
            Double bias_content = contentFuture.get();
            ParagraphAnalysisResult paragraphAnalysisResult = paragraphFuture.get();

            Double paragraph_reliability = paragraphAnalysisResult.getAverageReliability();

            double finalScore = (bias_title + bias_content + paragraph_reliability) / 3;

            return BiasAnalysisResult.builder()
                    .biasScore(finalScore)
                    .reliability(finalScore)
                    .paragraphReliabilities(paragraphAnalysisResult.getReliabilityScores())
                    .paragraphReasons(paragraphAnalysisResult.getBestMatches())
                    .build();

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Bias analysis failed", e);
        }
    }
}
