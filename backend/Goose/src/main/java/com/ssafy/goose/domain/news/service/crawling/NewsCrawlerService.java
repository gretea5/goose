package com.ssafy.goose.domain.news.service.crawling;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NewsCrawlerService {
    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    // ✅ AI 키워드 추출 API 주소
    private final String aiKeywordExtractionUrl = "http://i12d208.p.ssafy.io:5053/today-hot-keywords";

    /**
     * 🔹 1. 네이버 뉴스 API에서 최신 뉴스 제목 가져오기
     */
    public List<String> extractTrendingKeywords() {
        String url = "https://openapi.naver.com/v1/search/news.json?query=뉴스&display=50&sort=date";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        List<Map<String, Object>> newsItems = (List<Map<String, Object>>) response.getBody().get("items");
        List<String> titles = newsItems.stream()
                .map(item -> ((String) item.get("title")).replaceAll("<[^>]*>", "")) // ✅ HTML 태그 제거
                .collect(Collectors.toList());

        // 🔹 2. AI 기반 키워드 추출
        return extractKeywordsUsingAI(titles);
    }

    /**
     * 🔹 2. AI를 활용한 현재 HOT 키워드 추출
     */
    private List<String> extractKeywordsUsingAI(List<String> titles) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("titles", titles);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(aiKeywordExtractionUrl, HttpMethod.POST, entity, Map.class);
        if (response.getBody() == null || !response.getBody().containsKey("keywords")) {
            return Collections.emptyList();
        }

        return (List<String>) response.getBody().get("keywords");
    }

    /**
     * 🔹 3. 특정 키워드 기반 뉴스 검색
     */
    public Map<String, Object> getNews(String keyword, int displayCount) {
        String url = "https://openapi.naver.com/v1/search/news.json?query=" + keyword + "&display="+ displayCount +"&sort=date";
//        System.out.println("요청 API URL : " + url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        return response.getBody();
    }
}
