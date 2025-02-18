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

@Service
public class AnalyseByTitle {
    private final RestTemplate restTemplate = new RestTemplate();
//    private static final String TITLE_COMPARE_CONTENTS_API_URL = "http://i12d208.p.ssafy.io:5062/title-compare-contents";
    private static final String TITLE_COMPARE_CONTENTS_API_URL = "http://localhost:5062/title-compare-contents";

    public double checkTitleWithReference(String newsId, List<ReferenceNewsArticle> referenceNewsList) {
        try {
            int totalArticles = referenceNewsList.size();
            if (totalArticles == 0) return 0.0;

            ExecutorService executor = Executors.newFixedThreadPool(20); // 스레드풀 생성 (적절히 조절)

            List<CompletableFuture<Double>> futures = referenceNewsList.stream()
                    .map(referenceNews -> CompletableFuture.supplyAsync(() -> {
                        try {
//                            System.out.println("제목과 내용 임베딩 차이 구하기, newsId : " + newsId);
//                            System.out.println("제목과 내용 임베딩 차이 구하기, referenceNewsId : " + referenceNews.getId());

                            Map<String, String> requestBody = new HashMap<>();
                            requestBody.put("newsId", newsId);
                            requestBody.put("referenceNewsId", referenceNews.getId());

                            HttpHeaders headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            HttpEntity<String> requestEntity = new HttpEntity<>(new ObjectMapper().writeValueAsString(requestBody), headers);

                            ResponseEntity<String> response = restTemplate.postForEntity(TITLE_COMPARE_CONTENTS_API_URL, requestEntity, String.class);
                            Map<String, Object> responseBody = new ObjectMapper().readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});

                            double similarity_score = ((Number) responseBody.get("similarity_score")).doubleValue();

                            System.out.println("[제목분석] similarity_score : " + similarity_score);
                            return similarity_score;

                        } catch (Exception e) {
                            e.printStackTrace();
                            return 0.5; // 실패 시 기본값 반환
                        }
                    }, executor))
                    .toList();

            // 모든 병렬 작업 결과 수집
            double totalScore = futures.stream()
                    .map(CompletableFuture::join)
                    .mapToDouble(Double::doubleValue)
                    .sum();

            executor.shutdown();

            double averageScore = totalScore * 100 / totalArticles;
            System.out.println("Title 사용, 평균 Bias (0.0 ~ 100.0 범위) : " + averageScore);

            return averageScore;

        } catch (Exception e) {
            e.printStackTrace();
            return 50.0;
        }
    }
}
