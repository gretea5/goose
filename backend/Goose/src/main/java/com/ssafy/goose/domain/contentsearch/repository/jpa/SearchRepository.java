package com.ssafy.goose.domain.contentsearch.repository.jpa;

import com.ssafy.goose.domain.contentsearch.entity.Search;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchRepository extends JpaRepository<Search, Long> {

    Optional<Search> findByKeywords(String keyword);
    List<Search> findByKeywordsContaining(String keyword);
}
