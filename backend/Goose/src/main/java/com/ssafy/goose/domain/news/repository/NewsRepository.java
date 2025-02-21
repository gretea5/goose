package com.ssafy.goose.domain.news.repository;

import com.ssafy.goose.domain.news.entity.NewsArticle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends MongoRepository<NewsArticle, String> {

    // 제목에 특정 키워드가 포함된 뉴스 조회 (대소문자 구분 없음)
    List<NewsArticle> findByTitleContainingIgnoreCase(String keyword);

    // 특정 날짜의 뉴스 조회
    List<NewsArticle> findByPubDate(String pubDate);

    // 날짜 범위로 뉴스 조회
    List<NewsArticle> findByPubDateBetween(String startDate, String endDate);

    // 제목에서 정규식을 사용하여 특정 단어 포함된 뉴스 조회
    List<NewsArticle> findByTitleRegex(String regex);

    // 🔹 최신순으로 상위 10개 뉴스 조회
    List<NewsArticle> findTop10ByOrderByExtractedAtDesc();
}
