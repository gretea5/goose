package com.ssafy.goose.domain.news.repository;

import com.ssafy.goose.domain.contentsearch.dto.KeywordResponseDto;
import com.ssafy.goose.domain.contentsearch.service.KeywordService;
import com.ssafy.goose.domain.news.entity.ReferenceNewsArticle;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        // 키워드 추출
        KeywordResponseDto keywordResponse = keywordService.extractKeywords(content);
        String[] keywords = keywordResponse.getKeywords();
        System.out.println("키워드 : " + keywords[0] + " " + keywords[1] + " " + keywords[2]);

        Query query = new Query();

        // ✅ 3일 이내의 뉴스만 검색
        // query.addCriteria(Criteria.where("pubDate").gte(since));

        // ✅ OR 조건을 사용하여 title, description, content 중 하나라도 포함된 뉴스 검색
        List<Criteria> keywordCriteriaList = new ArrayList<>();
        for (String keyword : keywords) {
            keywordCriteriaList.add(new Criteria().orOperator(
                    Criteria.where("title").regex(".*" + keyword + ".*", "i"),
                    Criteria.where("description").regex(".*" + keyword + ".*", "i"),
                    Criteria.where("content").regex(".*" + keyword + ".*", "i")
            ));
        }

        if (!keywordCriteriaList.isEmpty()) {
            query.addCriteria(new Criteria().orOperator(keywordCriteriaList.toArray(new Criteria[0])));
        }

        // ✅ 검색 결과 최대 5개 제한
        query.limit(5);

        List<ReferenceNewsArticle> mongoData = mongoTemplate.find(query, ReferenceNewsArticle.class, "reference_news");

        // ✅ 검색된 데이터 개수 출력 (디버깅용)
        System.out.println("🔍 MongoDB에서 검색된 데이터: " + mongoData.size() + "개");

        return mongoData;
    }
}
