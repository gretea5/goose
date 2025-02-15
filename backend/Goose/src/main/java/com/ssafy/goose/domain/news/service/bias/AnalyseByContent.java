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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class AnalyseByContent {
    private final RestTemplate restTemplate = new RestTemplate();
    // private static final String CONTENT_COMPARE_CONTENTS_API_URL = "http://i12d208.p.ssafy.io:5062/paragraph-compare-contents";
    private static final String CONTENT_COMPARE_CONTENTS_API_URL = "http://localhost:5062/paragraph-compare-contents";

    public double checkContentWithReference(String newsId, List<ReferenceNewsArticle> referenceNewsList) {
        try {
            int totalArticles = referenceNewsList.size();
            if (totalArticles == 0) return 0.0;

            double totalScore = 0.0;

            for (ReferenceNewsArticle referenceNews : referenceNewsList) {
                // ✅ 문단 개수만큼 인덱스 동적 생성
                List<Integer> paragraphIndices = IntStream.range(0, referenceNews.getParagraphs().size())
                        .boxed()
                        .collect(Collectors.toList());

                // ✅ 요청 데이터 생성
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("newsId", newsId);
                requestBody.put("referenceNewsId", referenceNews.getId());
                requestBody.put("paragraphIndices", paragraphIndices);

                // ✅ HTTP 요청 설정
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> requestEntity = new HttpEntity<>(new ObjectMapper().writeValueAsString(requestBody), headers);

                // ✅ FastAPI 서버 호출
                ResponseEntity<String> response = restTemplate.postForEntity(CONTENT_COMPARE_CONTENTS_API_URL, requestEntity, String.class);

                // ✅ JSON 응답 파싱
                Map<String, Object> responseBody = new ObjectMapper().readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});

                List<Map<String, Object>> similarities = (List<Map<String, Object>>) responseBody.get("similarities");

                // ✅ 최대 유사도 점수 계산
                double maxSimilarityScore = similarities.stream()
                        .mapToDouble(s -> ((Number) s.get("similarity")).doubleValue())
                        .max()
                        .orElse(0.0);

                System.out.println("[내용분석] max similarity_score : " + maxSimilarityScore);

                // ✅ 점수 합산
                totalScore += maxSimilarityScore;
            }

            // ✅ 평균 점수 계산 (0.0 ~ 1.0 -> 0.0 ~ 100.0 변환)
            double averageScore = totalScore * 100 / totalArticles;
            System.out.println("Content 사용, 평균 Bias (0.0 ~ 100.0 범위): " + averageScore);

            return averageScore;

        } catch (Exception e) {
            e.printStackTrace();
            return 50.0; // 오류 발생 시 50 반환
        }
    }
}
