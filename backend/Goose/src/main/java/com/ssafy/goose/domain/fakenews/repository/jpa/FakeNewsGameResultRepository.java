package com.ssafy.goose.domain.fakenews.repository.jpa;

import com.ssafy.goose.domain.fakenews.entity.FakeNewsGameResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FakeNewsGameResultRepository extends JpaRepository<FakeNewsGameResult, Long> {
    List<FakeNewsGameResult> findByUsername(String username);
}
