package com.ssafy.goose.domain.contentsearch.service;

import com.ssafy.goose.domain.contentsearch.dto.KeywordRequestDto;
import com.ssafy.goose.domain.contentsearch.dto.KeywordResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Service
public class KeywordService {

    private final RestTemplate restTemplate = new RestTemplate();
    private String fastApiUrl = "http://i12d208.p.ssafy.io:6050";

    public KeywordResponseDto extractKeywords(String text) {
        String url = fastApiUrl + "/content_keywords";

        KeywordRequestDto requestDto = new KeywordRequestDto();
        requestDto.setText(text);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<KeywordRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);

        ResponseEntity<KeywordResponseDto> responseEntity =
                restTemplate.exchange(url, HttpMethod.POST, requestEntity, KeywordResponseDto.class);

        return responseEntity.getBody();
    }
}
