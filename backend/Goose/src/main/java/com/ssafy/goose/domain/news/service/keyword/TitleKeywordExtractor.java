package com.ssafy.goose.domain.news.service.keyword;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TitleKeywordExtractor {

    // ✅ 한국어 명사 추출을 위한 정규식 (간단 버전)
    private static final Pattern NOUN_PATTERN = Pattern.compile("\\b[가-힣]+\\b");

    public List<String> extractTopKeywords(String text, int limit) {
        // 🔹 1️⃣ 명사 찾기
        Matcher matcher = NOUN_PATTERN.matcher(text);
        Map<String, Integer> wordCount = new HashMap<>();

        while (matcher.find()) {
            String word = matcher.group();
            wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
        }

        // 🔹 2️⃣ 빈도수 기준 정렬 후 상위 n개 반환
        return wordCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // 빈도순 정렬
                .map(Map.Entry::getKey)
                .limit(limit)
                .collect(Collectors.toList());
    }
}
