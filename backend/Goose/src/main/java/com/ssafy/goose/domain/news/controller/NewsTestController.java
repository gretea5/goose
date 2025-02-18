package com.ssafy.goose.domain.news.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "News Test API", description = "FastAPI 모델 연동 테스트")
@RestController
@RequestMapping("/api/news-test")
@RequiredArgsConstructor
public class NewsTestController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String FASTAPI_URL = "http://54.180.132.214:8000/predict"; // https인지도 확인 필요

    @Operation(summary = "FastAPI 예측 요청 테스트", description = "FastAPI 서버로 뉴스 데이터를 보내 예측 결과(confidence, sentence_scores)를 반환받습니다.")
    @PostMapping("/predict")
    public ResponseEntity<Map<String, Object>> testPrediction(
            @RequestBody Map<String, Object> request
    ) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "Mozilla/5.0"); // User-Agent 추가

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    FASTAPI_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}

