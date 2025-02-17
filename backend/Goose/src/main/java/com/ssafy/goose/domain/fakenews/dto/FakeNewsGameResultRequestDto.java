package com.ssafy.goose.domain.fakenews.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FakeNewsGameResultRequestDto {
    private String newsId;       // 뉴스 아이디
    private String userChoice;   // "허위정보", "과장보도", "클릭베이트"
    private long dwellTime;      // 체류 시간 (밀리초)
    private int totalQuestions;  // 지금까지 푼 문제 수
    private int correctCount;    // 맞춘 문제 수
}
