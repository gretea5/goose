package com.ssafy.goose.domain.warning.repository;

import com.ssafy.goose.domain.warning.entity.WarningNewsAgency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarningNewsAgencyRepository extends JpaRepository<WarningNewsAgency, Long> {
    List<WarningNewsAgency> findAllByOrderByWarningCountDesc();
    WarningNewsAgency findByNewsAgency(String newsAgency);
}
