package com.ssafy.goose.domain.fakenews.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@Entity
@Table(name = "fake_news_game")
public class FakeNewsGameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;        // 로그인 사용자는 username, 비로그인의 경우 "guest"
    private String newsId;          // 해당 게임 문제(가짜 뉴스)의 ID (MongoDB의 FakeNews id)
    private String userChoice;      // 사용자가 선택한 옵션 ("허위 정보", "과장 보도", "클릭베이트")
    private Boolean correct;        // 정답 여부 (true/false)
    private Long dwellTime;         // 해당 뉴스에서 머문 체류 시간 (밀리초)
    private int totalQuestions;     // 지금까지 푼 총 문제 수
    private int correctCount;       // 지금까지 맞춘 정답 수
    private LocalDateTime solvedAt; // 문제 풀이 시각 (타임스탬프)
}
