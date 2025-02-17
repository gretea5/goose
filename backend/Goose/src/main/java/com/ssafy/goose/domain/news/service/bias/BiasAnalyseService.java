package com.ssafy.goose.domain.news.service.bias;

import com.ssafy.goose.domain.news.entity.ReferenceNewsArticle;
import com.ssafy.goose.domain.news.repository.ReferenceNewsCustomRepository;
import com.ssafy.goose.domain.news.service.EmbeddingStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class BiasAnalyseService {

    private final ReferenceNewsCustomRepository referenceNewsCustomRepository;
    private final AnalyseByTitle analyseByTitle;
    private final AnalyseByContent analyseByContent;
    private final AnalyzeParagraph analyzeParagraph;
    private final EmbeddingStorageService embeddingStorageService;

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

        // 비동기 병렬 임베딩 저장
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

        // 분석 실행 (레퍼런스 뉴스 ID 넘기기)
        return analyzeBiasWithReference(id, title, content, paragraphs, referenceNewsList);
    }

    public BiasAnalysisResult analyzeBiasWithReference(String id, String title, String content, List<String> paragraphs, List<ReferenceNewsArticle> referenceNewsList) {
        System.out.println("analyzeBiasWithReference 수행, title : " + title);

        try {
            CompletableFuture<Double> titleFuture = CompletableFuture.supplyAsync(
                    () -> analyseByTitle.checkTitleWithReference(id, referenceNewsList), executorService
            );

            CompletableFuture<Double> contentFuture = CompletableFuture.supplyAsync(
                    () -> analyseByContent.checkContentWithReference(id, referenceNewsList), executorService
            );

            List<String> referenceParagraphIds = referenceNewsList.stream()
                    .flatMap(ref -> IntStream.range(0, ref.getParagraphs().size())
                            .mapToObj(i -> ref.getId() + "_p_" + i))
                    .collect(Collectors.toList());

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
