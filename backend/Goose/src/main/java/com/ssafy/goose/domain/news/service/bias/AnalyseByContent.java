package com.ssafy.goose.domain.news.service.bias;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.goose.domain.news.entity.ReferenceNewsArticle;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class AnalyseByContent {
    private final RestTemplate restTemplate = new RestTemplate();
//    private static final String CONTENT_COMPARE_CONTENTS_API_URL = "http://i12d208.p.ssafy.io:5062/paragraph-compare-contents";
    private static final String CONTENT_COMPARE_CONTENTS_API_URL = "http://localhost:5062/paragraph-compare-contents";

    public double checkContentWithReference(String newsId, List<ReferenceNewsArticle> referenceNewsList) {
        if (referenceNewsList.isEmpty()) {
            return 0.0;
        }

        // ✅ 병렬 처리를 위한 ExecutorService 생성 (스레드풀)
        ExecutorService executor = Executors.newFixedThreadPool(20); // 적절히 조절 가능

        try {
            // ✅ 비동기 작업 생성
            List<CompletableFuture<Double>> futures = referenceNewsList.stream()
                    .map(referenceNews -> CompletableFuture.supplyAsync(() -> {
                        try {
                            // 문단 인덱스 생성
                            List<Integer> paragraphIndices = IntStream.range(0, referenceNews.getParagraphs().size())
                                    .boxed()
                                    .collect(Collectors.toList());

                            // 요청 데이터 생성
                            Map<String, Object> requestBody = new HashMap<>();
                            requestBody.put("newsId", newsId);
                            requestBody.put("referenceNewsId", referenceNews.getId());
                            requestBody.put("paragraphIndices", paragraphIndices);

                            HttpHeaders headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            HttpEntity<String> requestEntity = new HttpEntity<>(new ObjectMapper().writeValueAsString(requestBody), headers);

                            // FastAPI 서버 호출
                            ResponseEntity<String> response = restTemplate.postForEntity(CONTENT_COMPARE_CONTENTS_API_URL, requestEntity, String.class);

                            // 응답 파싱
                            Map<String, Object> responseBody = new ObjectMapper().readValue(response.getBody(), new TypeReference<Map<String, Object>>() {
                            });

                            List<Map<String, Object>> similarities = (List<Map<String, Object>>) responseBody.get("similarities");

                            // 최대 유사도 점수 계산
                            double maxSimilarityScore = similarities.stream()
                                    .mapToDouble(s -> ((Number) s.get("similarity")).doubleValue())
                                    .max()
                                    .orElse(0.0);

                            System.out.println("[내용분석] max similarity_score : " + maxSimilarityScore + " (referenceNewsId: " + referenceNews.getId() + ")");

                            return maxSimilarityScore;

                        } catch (Exception e) {
                            e.printStackTrace();
                            return 0.5; // 오류 발생 시 기본값
                        }
                    }, executor))
                    .toList();

            // ✅ 모든 작업 완료 대기 및 결과 수집
            double totalScore = futures.stream()
                    .map(CompletableFuture::join)
                    .mapToDouble(Double::doubleValue)
                    .sum();

            // ✅ 평균 점수 계산 (0.0 ~ 1.0 -> 0.0 ~ 100.0 변환)
            double averageScore = totalScore * 100 / referenceNewsList.size();
            System.out.println("Content 사용, 평균 Bias (0.0 ~ 100.0 범위): " + averageScore);

            return averageScore;

        } catch (Exception e) {
            e.printStackTrace();
            return 50.0; // 오류 발생 시 50 반환
        } finally {
            // ✅ ExecutorService 종료 (자원 반납)
            executor.shutdown();
        }
    }
}
