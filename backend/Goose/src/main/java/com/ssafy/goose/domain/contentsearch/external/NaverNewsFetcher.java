package com.ssafy.goose.domain.contentsearch.external;

import com.ssafy.goose.domain.contentsearch.dto.NewsResponseDto;
import com.ssafy.goose.domain.news.service.crawling.NewsContentScraping;
import com.ssafy.goose.domain.news.service.paragraph.NewsParagraphSplitService;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
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
        WebClient webClient = WebClient.builder()
                .baseUrl(NAVER_NEWS_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-Naver-Client-Id", clientId)
                .defaultHeader("X-Naver-Client-Secret", clientSecret)
                .build();

        String query = String.join(" ", keywords);
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        String responseBody = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("query", encodedQuery)
                        .queryParam("display", 20)
                        .build()
                )
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JSONObject jsonResponse = new JSONObject(responseBody);
        JSONArray items = jsonResponse.getJSONArray("items");
        System.out.println("네이버 뉴스 1차 검색 결과 수 : " + items.length());

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
                }))
                .collect(Collectors.toList());

        List<NewsResponseDto> newsList = futures.stream()
                .map(CompletableFuture::join)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        System.out.println("네이버로부터 검색 완료, 갯수 : " + newsList.size());
        return newsList;
    }
}
