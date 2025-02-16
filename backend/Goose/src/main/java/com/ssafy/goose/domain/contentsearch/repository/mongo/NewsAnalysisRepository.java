package com.ssafy.goose.domain.contentsearch.repository.mongo;

import com.ssafy.goose.domain.contentsearch.entity.NewsAnalysis;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NewsAnalysisRepository extends MongoRepository<NewsAnalysis, String> {

    // 특정 사용자의 분석 기록을 조회할 때
    List<NewsAnalysis> findByUsername(String username);
}
