package com.ssafy.goose.domain.news.service.bias;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AnalyzeParagraph {

    // ✅ FastAPI 서버 URL (EC2 배포 시 주소 변경 필요)
    private static final String FASTAPI_URL = "http://i12d208.p.ssafy.io:5061/news/reliability";
//    private static final String FASTAPI_URL = "http://localhost:5061/news/reliability";
    private final RestTemplate restTemplate;

    public AnalyzeParagraph() {
        this.restTemplate = new RestTemplate();
    }

    public ParagraphAnalysisResult analyze(String title, List<String> paragraphs, List<String> referenceParagraphIds) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("news", Map.of(
                "title", title,
                "paragraphs", paragraphs
        ));
        requestBody.put("referenceParagraphIds", referenceParagraphIds);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(FASTAPI_URL, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Double> reliabilityScores = (List<Double>) response.getBody().get("paragraph_reliability_scores");
                List<String> bestMatches = (List<String>) response.getBody().get("best_evidence_paragraphs");

                System.out.println("✅ 문단별 신뢰도 및 분석 근거 추출 완료");
                return new ParagraphAnalysisResult(reliabilityScores, bestMatches);
            }
        } catch (Exception e) {
            System.err.println("❌ FastAPI 요청 실패: " + e.getMessage());
        }

        return new ParagraphAnalysisResult(List.of(50.0), List.of());
    }

}
