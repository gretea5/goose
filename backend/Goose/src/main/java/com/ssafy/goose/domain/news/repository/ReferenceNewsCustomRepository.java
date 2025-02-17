package com.ssafy.goose.domain.news.repository;

import com.ssafy.goose.domain.contentsearch.dto.KeywordResponseDto;
import com.ssafy.goose.domain.contentsearch.service.KeywordService;
import com.ssafy.goose.domain.news.entity.ReferenceNewsArticle;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
public class ReferenceNewsCustomRepository {

    private final MongoTemplate mongoTemplate;
    private final KeywordService keywordService;

    public ReferenceNewsCustomRepository(MongoTemplate mongoTemplate, KeywordService keywordService) {
        this.mongoTemplate = mongoTemplate;
        this.keywordService = keywordService;
    }

    public List<ReferenceNewsArticle> findNewsByKeywords(String title, String content) {
        // ✅ 키워드 추출
        KeywordResponseDto keywordResponse = keywordService.extractKeywords(content);
        String[] keywords = keywordResponse.getKeywords();
        System.out.println("키워드 : " + String.join(", ", keywords));

        // ✅ 검색 쿼리 설정
        Query query = new Query();

        // ✅ 여러 키워드를 공백으로 결합해 텍스트 검색에 활용
        String searchQuery = String.join(" ", keywords);

        // ✅ 3일 이내 뉴스만 검색 (pubDate가 String이면 비교를 위해 LocalDateTime 변환 필요)
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        // pubDate를 LocalDateTime으로 파싱하는 방식 (예: "2024-02-16T12:34:56")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        query.addCriteria(
                new Criteria().andOperator(
                        Criteria.where("$text").is(new org.bson.Document("$search", searchQuery)),
                        Criteria.where("pubDate").gte(threeDaysAgo.format(formatter))
                )
        );

        // ✅ 점수 기준 정렬 (검색 연관도 높은 순서)
        query.with(Sort.by(Sort.Order.desc("score")));

        // ✅ 최대 5개 제한
        query.limit(5);

        // ✅ 데이터 조회
        List<ReferenceNewsArticle> mongoData = mongoTemplate.find(query, ReferenceNewsArticle.class, "reference_news");

        System.out.println("🔍 MongoDB에서 검색된 데이터: " + mongoData.size() + "개");

        return mongoData;
    }
}
