package com.ssafy.goose.domain.news.service.bias;

import com.ssafy.goose.domain.news.entity.ReferenceNewsArticle;
import com.ssafy.goose.domain.news.repository.ReferenceNewsCustomRepository;
import com.ssafy.goose.domain.news.service.EmbeddingStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class BiasAnalyseService {

    private final ReferenceNewsCustomRepository referenceNewsCustomRepository;
    private final AnalyseByTitle analyseByTitle;
    private final AnalyseByContent analyseByContent;
    private final AnalyzeParagraph analyzeParagraph;
    private final EmbeddingStorageService embeddingStorageService;

    // ✅ 병렬 작업용 스레드 풀 설정 (코어: 5, 최대: 10, 큐: 100)
    private final ExecutorService executorService = new ThreadPoolExecutor(
            5, 10, 30L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            new CustomizableThreadFactory("bias-analyse-")
    );

    public BiasAnalysisResult analyzeBias(String id, String title, String content, List<String> paragraphs) {
        System.out.println("analyzeBias 수행, title : " + title);

        // 2. 레퍼런스 뉴스 검색
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

        // 3. 임베딩 저장 (동기 처리)
//        for (ReferenceNewsArticle referenceNews : referenceNewsList) {
//            embeddingStorageService.storeReferenceNews(
//                    EmbeddingStorageService.EmbeddingRequest.builder()
//                            .id(referenceNews.getId())
//                            .title(referenceNews.getTitle())
//                            .content(referenceNews.getContent())
//                            .paragraphs(referenceNews.getParagraphs())
//                            .pubDate(referenceNews.getPubDate())
//                            .build()
//            );
//            System.out.println("referenceNews 임베딩 저장");
//        }

        // 3. 임베딩 저장 (비동기 병렬 처리)
        ExecutorService executor = Executors.newFixedThreadPool(20); // 병렬 처리 스레드 수 설정

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

        // 모든 작업 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        executor.shutdown();

        try {
            // 4, 5, 6 병렬 실행
            CompletableFuture<Double> titleFuture = CompletableFuture.supplyAsync(
                    () -> analyseByTitle.checkTitleWithReference(id, referenceNewsList), executorService
            );

            CompletableFuture<Double> contentFuture = CompletableFuture.supplyAsync(
                    () -> analyseByContent.checkContentWithReference(id, referenceNewsList), executorService
            );

            CompletableFuture<ParagraphAnalysisResult> paragraphFuture = CompletableFuture.supplyAsync(
                    () -> analyzeParagraph.analyze(title, paragraphs), executorService
            );

            // ✅ 병렬 작업 완료 대기
            Double bias_title = titleFuture.get();  // 블로킹 (결과 기다림)
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

    public BiasAnalysisResult analyzeBiasWithReference(String id, String title, String content, List<String> paragraphs, List<ReferenceNewsArticle> referenceNewsList) {
        System.out.println("analyzeBias 수행, title : " + title);

        try {
            // 4, 5, 6 병렬 실행
            CompletableFuture<Double> titleFuture = CompletableFuture.supplyAsync(
                    () -> analyseByTitle.checkTitleWithReference(id, referenceNewsList), executorService
            );

            CompletableFuture<Double> contentFuture = CompletableFuture.supplyAsync(
                    () -> analyseByContent.checkContentWithReference(id, referenceNewsList), executorService
            );

            CompletableFuture<ParagraphAnalysisResult> paragraphFuture = CompletableFuture.supplyAsync(
                    () -> analyzeParagraph.analyze(title, paragraphs), executorService
            );

            // ✅ 병렬 작업 완료 대기
            Double bias_title = titleFuture.get();  // 블로킹 (결과 기다림)
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

