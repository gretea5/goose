package com.ssafy.goose.domain.contentsearch.external;

import com.ssafy.goose.domain.contentsearch.dto.NewsResponseDto;
import com.ssafy.goose.domain.news.service.crawling.NewsContentScraping;
import com.ssafy.goose.domain.news.service.paragraph.NewsParagraphSplitService;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class NaverNewsFetcher {

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    private static final String NAVER_NEWS_URL = "https://openapi.naver.com/v1/search/news.json?query=";

    private final NewsAgencyExtractor newsAgencyExtractor;
    private final NewsContentScraping newsContentScraping;
    private final NewsParagraphSplitService newsParagraphSplitService;

    public List<NewsResponseDto> fetchNaverNews(String[] keywords) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                NAVER_NEWS_URL + String.join(" ", keywords) + "&display=20",
                HttpMethod.GET,
                entity,
                String.class
        );

        JSONObject jsonResponse = new JSONObject(response.getBody());
        JSONArray items = jsonResponse.getJSONArray("items");

        ExecutorService executor = Executors.newFixedThreadPool(5);

        List<CompletableFuture<NewsResponseDto>> futures = IntStream.range(0, items.length())
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                    try {
                        JSONObject jsonObject = items.getJSONObject(i);

                        String originalLink = jsonObject.optString("originallink", jsonObject.optString("link", ""));
                        String naverLink = jsonObject.optString("link", "");
                        String pubDate = jsonObject.optString("pubDate", "");

                        String newsAgency = newsAgencyExtractor.extractNewsAgency(originalLink.isEmpty() ? naverLink : originalLink);

                        Map<String, Object> scrapedData = newsContentScraping.extractArticle(naverLink);
                        if (scrapedData == null || !scrapedData.containsKey("text")) return null;

                        String cleanTitle = (String) scrapedData.get("title");
                        String content = (String) scrapedData.get("text");
                        String topImage = (String) scrapedData.get("image");

                        if (content.length() < 100) return null;

                        List<String> paragraphs = newsParagraphSplitService.getSplitParagraphs(content);

                        return NewsResponseDto.builder()
                                .id(null)
                                .title(cleanTitle)
                                .originalLink(originalLink)
                                .naverLink(naverLink)
                                .description(jsonObject.optString("description", ""))
                                .pubDate(pubDate)
                                .content(content)
                                .paragraphs(paragraphs)
                                .paragraphReliabilities(new ArrayList<>())
                                .paragraphReasons(new ArrayList<>())
                                .topImage(topImage)
                                .extractedAt(LocalDateTime.now())
                                .biasScore(0.0)
                                .reliability(50.0)
                                .build();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }, executor))
                .toList();

        List<NewsResponseDto> newsList = futures.stream()
                .map(CompletableFuture::join)
                .filter(dto -> dto != null)
                .toList();

        executor.shutdown();

        return newsList;
    }
}
