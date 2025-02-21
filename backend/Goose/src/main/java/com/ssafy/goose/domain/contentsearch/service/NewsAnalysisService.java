package com.ssafy.goose.domain.contentsearch.service;

import com.ssafy.goose.domain.contentsearch.entity.NewsAnalysis;
import com.ssafy.goose.domain.contentsearch.repository.mongo.NewsAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsAnalysisService {

    private final NewsAnalysisRepository newsAnalysisRepository;

    // 사용자별 분석 기록 저장
    public NewsAnalysis saveAnalysis(NewsAnalysis newsAnalysis) {
        return newsAnalysisRepository.save(newsAnalysis);
    }

    // 사용자별 분석 기록 조회
    public List<NewsAnalysis> getAnalysesByUsername(String username) {
        return newsAnalysisRepository.findByUsername(username);
    }
}
