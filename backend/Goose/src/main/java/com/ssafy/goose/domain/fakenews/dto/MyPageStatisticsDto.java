package com.ssafy.goose.domain.fakenews.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class MyPageStatisticsDto {

    private String nickname;                 // 닉네임
    private int totalQuestions;              // 전체 게임 횟수
    private int correctCount;                // 맞춘 게임 개수
    private double correctRate;              // 정답률 (정답 수 / 전체 게임 수 * 100)
    private List<GameRecordDTO> gameRecords; // 사용자 게임 참여 기록


    @Getter @Setter
    public static class GameRecordDTO {

        private LocalDateTime solvedAt;      // 문제를 풀었던 시각
    }
}
