package com.ssafy.goose.domain.fakenews.repository.mongo;

import com.ssafy.goose.domain.fakenews.entity.FakeNews;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FakeNewsRepository extends MongoRepository<FakeNews, String> {

}
