package com.ssafy.goose.domain.contentsearch.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KeywordRequestDto {

    private String text;         // 안드로이드에서 받아온 텍스트
    private String analysisType; // "content", "image", "url" 중 하나

    public void setText(String text) {
        if (text != null) {
            this.text = cleanText(text);
        }
    }

    // 특수문자, 따옴표, 줄바꿈 제거
    private String cleanText(String text) {
        return text
                .replaceAll("\"", "'")
                .replaceAll("[\n\r]", " ")
                .replaceAll("\\s+", " ")
                .replaceAll("[^가-힣a-zA-Z0-9,.!? ]", "")
                .trim();
    }
}
