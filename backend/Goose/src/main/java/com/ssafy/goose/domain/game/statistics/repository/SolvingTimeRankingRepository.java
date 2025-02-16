package com.ssafy.goose.domain.game.statistics.repository;

import com.ssafy.goose.domain.game.statistics.entity.SolvingTimeRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SolvingTimeRankingRepository extends JpaRepository<SolvingTimeRanking, Long> {
    // 특정 문제의 상위 3명 랭킹 조회
    @Query("SELECT s FROM SolvingTimeRanking s WHERE s.questionId = :questionId ORDER BY s.solvingTime ASC LIMIT 3")
    List<SolvingTimeRanking> findTop3ByQuestionId(@Param("questionId") Long questionId);

    // 특정 사용자의 특정 문제 풀이 시간 순위 조회
    @Query("SELECT COUNT(s) + 1 FROM SolvingTimeRanking s WHERE s.questionId = :questionId AND s.solvingTime < (SELECT s2.solvingTime FROM SolvingTimeRanking s2 WHERE s2.userId = :userId AND s2.questionId = :questionId)")
    Long getUserRankForQuestion(@Param("userId") Long userId, @Param("questionId") Long questionId);
}
