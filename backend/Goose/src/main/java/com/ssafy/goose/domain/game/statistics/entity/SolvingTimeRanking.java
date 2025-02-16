package com.ssafy.goose.domain.game.statistics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder  // Builder 패턴을 사용하기 위한 어노테이션 추가
@Table(name = "solving_time_ranking")
public class SolvingTimeRanking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;  // 사용자 ID

    @Column(nullable = false)
    private String nickname;  // 닉네임

    @Column(nullable = false)
    private Long questionId;  // 문제 ID

    @Column(nullable = false)
    private Duration solvingTime;  // 문제 풀이 시간
}
