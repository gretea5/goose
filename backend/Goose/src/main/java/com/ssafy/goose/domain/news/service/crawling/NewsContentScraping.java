package com.ssafy.goose.domain.news.service.crawling;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class NewsContentScraping {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String FASTAPI_NEWS_URL = "http://i12d208.p.ssafy.io:5050/extract"; // ✅ 뉴스 크롤링 FastAPI 서버 URL

    /**
     * 🔹 FastAPI (Newspaper3k) 호출하여 뉴스 본문과 대표 이미지를 가져옴
     */
    public Map<String, Object> extractArticle(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("url", url);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(FASTAPI_NEWS_URL, HttpMethod.POST, entity, Map.class);
            Map<String, Object> result = response.getBody();

            if (result != null) {
                // 🔹 제목(title) HTML 엔티티 디코딩 + HTML 태그 제거
                String rawTitle = (String) result.get("title");
                if (rawTitle != null) {
                    String cleanTitle = cleanHtml(rawTitle);
                    result.put("title", cleanTitle);
                }

                // 🔹 본문(content) HTML 엔티티 디코딩 + HTML 태그 제거
                String rawContent = (String) result.get("text");
                if (rawContent != null) {
                    String cleanContent = cleanHtml(rawContent);
                    result.put("text", cleanContent);
                }

                // 🔹 설명(description) HTML 엔티티 디코딩 + HTML 태그 제거
                String rawDescription = (String) result.get("description");
                if (rawDescription != null) {
                    String cleanDescription = cleanHtml(rawDescription);
                    result.put("description", cleanDescription);
                }
            }

            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * HTML 엔티티 디코딩 및 HTML 태그 제거 유틸리티 메서드
     */
    private String cleanHtml(String htmlText) {
        String unescaped = StringEscapeUtils.unescapeHtml4(htmlText); // HTML 엔티티 디코딩
        return Jsoup.parse(unescaped).text(); // HTML 태그 제거
    }
}
