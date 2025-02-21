package com.ssafy.goose.domain.news.service.airate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiRateService {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String AI_MODEL_1_URL = "http://54.180.132.214:8000/predict";
    private static final String AI_MODEL_2_URL = "http://54.180.132.214:8001/analyze";

    public Double calculateAiRate(String title, List<String> paragraphs) {
        Double rate1 = getConfidenceRate(title, paragraphs);
        Double rate2 = getCoherenceRate(paragraphs);

        if (rate1 == null || rate2 == null) {
            return null;
        }

        return (rate1 + rate2) / 2.0;
    }

    private Double getConfidenceRate(String title, List<String> paragraphs) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new HashMap<>();
            payload.put("title", title);
            payload.put("paragraphs", paragraphs);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(AI_MODEL_1_URL, requestEntity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("confidence")) {
                Double confidence = ((Number) responseBody.get("confidence")).doubleValue();
                return 1 - confidence;
            }
        } catch (Exception e) {
            System.out.println("[ERROR] getConfidenceRate() - AI 모델 1 호출 실패: " + e.getMessage());
        }
        return null;
    }

    private Double getCoherenceRate(List<String> paragraphs) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new HashMap<>();
            payload.put("paragraphs", paragraphs);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(AI_MODEL_2_URL, requestEntity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("coherence_scores")) {
                List<Number> scores = (List<Number>) responseBody.get("coherence_scores");
                Double minScore = scores.stream().mapToDouble(Number::doubleValue).min().orElse(1.0);
                return 1 - minScore;
            }
        } catch (Exception e) {
            System.out.println("[ERROR] getCoherenceRate() - AI 모델 2 호출 실패: " + e.getMessage());
        }
        return null;
    }
}
