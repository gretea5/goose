package com.ssafy.goose.domain.contentsearch.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import com.ssafy.goose.domain.contentsearch.entity.News;
import java.util.List;

public interface ContentNewsRepository extends MongoRepository<News, String> {

    // keyword가 뉴스 title, description, content에 있으면 검색
    @Query("{ $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'description': { $regex: ?0, $options: 'i' } }, { 'content': { $regex: ?0, $options: 'i' } } ] }")
    List<News> searchByTitleOrDescriptionOrContent(String keyword);
}
