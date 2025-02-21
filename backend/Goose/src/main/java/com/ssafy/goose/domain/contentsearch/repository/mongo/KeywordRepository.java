package com.ssafy.goose.domain.contentsearch.repository.mongo;

import com.ssafy.goose.domain.contentsearch.entity.Content;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KeywordRepository extends MongoRepository<Content, String> {

    Optional<Content> findByKeyword(String keyword);
    List<Content> findByKeywordContaining(String keyword);
}
