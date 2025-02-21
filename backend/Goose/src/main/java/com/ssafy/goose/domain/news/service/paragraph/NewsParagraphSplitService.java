package com.ssafy.goose.domain.news.service.paragraph;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NewsParagraphSplitService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String fastApiUrl = "http://i12d208.p.ssafy.io:5052/split-paragraphs"; // FastAPI 서버 URL

    public List<String> getSplitParagraphs(String content) {
        // 🔹 FastAPI로 뉴스 본문을 전송
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("content", content);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(fastApiUrl, HttpMethod.POST, requestEntity, Map.class);

        // 🔹 분리된 문단 리스트 반환
        if (response.getBody() != null && response.getBody().containsKey("paragraphs")) {
            return (List<String>) response.getBody().get("paragraphs");
        }

        return List.of(content); // 실패하면 원본을 그대로 반환
    }
}
