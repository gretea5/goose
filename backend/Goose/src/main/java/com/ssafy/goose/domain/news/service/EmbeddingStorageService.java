package com.ssafy.goose.domain.news.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingStorageService {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String STORE_NEWS_URL = "http://localhost:5063/store-news";
    private final String STORE_REFERENCE_NEWS_URL = "http://localhost:5063/store-reference-news";

    public void storeNews(EmbeddingRequest request) {
        sendPostRequest(STORE_NEWS_URL, request.toMap());
    }

    public void storeReferenceNews(EmbeddingRequest request) {
        sendPostRequest(STORE_REFERENCE_NEWS_URL, request.toMap());
    }

    private void sendPostRequest(String url, Map<String, Object> requestBody) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            System.out.println("요청 성공: " + response.getBody());
        } catch (Exception e) {
            System.err.println("요청 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static class EmbeddingRequest {
        private String id;
        private String title;
        private String content;
        private List<String> paragraphs;
        private String pubDate;

        public static Builder builder() {
            return new Builder();
        }

        public Map<String, Object> toMap() {
            return Map.of(
                    "id", id,
                    "title", title,
                    "content", content,
                    "paragraphs", paragraphs,
                    "pubDate", pubDate
            );
        }

        public static class Builder {
            private final EmbeddingRequest request = new EmbeddingRequest();

            public Builder id(String id) {
                request.id = id;
                return this;
            }

            public Builder title(String title) {
                request.title = title;
                return this;
            }

            public Builder content(String content) {
                request.content = content;
                return this;
            }

            public Builder paragraphs(List<String> paragraphs) {
                request.paragraphs = paragraphs;
                return this;
            }

            public Builder pubDate(String pubDate) {
                request.pubDate = pubDate;
                return this;
            }

            public EmbeddingRequest build() {
                return request;
            }
        }
    }
}
