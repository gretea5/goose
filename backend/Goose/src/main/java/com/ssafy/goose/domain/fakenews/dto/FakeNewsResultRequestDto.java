package com.ssafy.goose.domain.fakenews.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FakeNewsResultRequestDto {
    private String newsId;       // 뉴스 아이디
    private String userChoice;   // "허위정보", "과장보도", "클릭베이트"
    private long dwellTime;      // 체류 시간 (밀리초)
    private String nickname;     // 비로그인은 null
}
