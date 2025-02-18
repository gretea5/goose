package com.ssafy.goose.domain.fakenews.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class UserGameResultDto {

    private String userChoice;            // 사용자가 선택한 답
    private Boolean correct;              // 정답 여부
    private Long dwellTime;               // 체류 시간 (비로그인은 null)
    private String nickname;              // 닉네임
    private LocalDateTime solvedAt;       // 문제 풀이 시각 (비로그인은 null)
    private List<RankingDTO> Ranking;     // 해당 뉴스의 체류 시간 랭킹 정보 (Top 3)


    @Getter @Setter
    public static class RankingDTO {

        private String nickname;          // 랭킹 닉네임
        private long dwellTime;           // 랭킹 체류 시간 (null 불가)
    }
}
