package com.ssafy.goose.domain.news.repository;

import com.ssafy.goose.domain.news.entity.ReferenceNewsArticle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReferenceNewsRepository extends MongoRepository<ReferenceNewsArticle, String> {

    // ✅ 특정 키워드가 포함된 참고 뉴스 검색
    List<ReferenceNewsArticle> findByTitleContainingIgnoreCase(String keyword);

    // ✅ 3일 이내 뉴스 검색
    List<ReferenceNewsArticle> findByPubDateAfter(Date since);
}
