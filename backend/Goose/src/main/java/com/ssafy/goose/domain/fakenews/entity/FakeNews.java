package com.ssafy.goose.domain.fakenews.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Getter @Setter
@Document(collection = "fake_news_game")
public class FakeNews {

    @Id
    private String id;                   // 뉴스 고유 번호
    private String title;                // 뉴스 제목
    private String content;              // 뉴스 내용 (800자 이상)
    private String fakeReason;           // 정답인 이유 (ex. "이 기사가 과장 보도인 이유는 …")
    private String correctAnswer;        // 정답: "허위정보", "과장보도", "클릭베이트"
    private String imageUrl;             // 관련 이미지 URL
    private String newsDate;             // 뉴스 날짜

    private Map<String, Integer> voteCounts = new HashMap<>();          // 각 선택지 별 투표수
    private Map<String, Double> selectionPercentages = new HashMap<>(); // 선택지 별 비율
    private List<Ranking> dwellTimeRanking = new ArrayList<>();         // 체류 시간 랭킹 Top3


    public FakeNews() {

        // 초기 투표수 0으로 설정
        voteCounts.put("허위정보", 0);
        voteCounts.put("과장보도", 0);
        voteCounts.put("클릭베이트", 0);

        // 초기 비율 0%로 설정
        selectionPercentages.put("허위정보", 0.0);
        selectionPercentages.put("과장보도", 0.0);
        selectionPercentages.put("클릭베이트", 0.0);
    }

    @Getter
    @Setter
    public static class Ranking {

        private String nickname; // 사용자 닉네임
        private long dwellTime;  // 체류 시간 (밀리초)

        public Ranking() {

        }

        public Ranking(String nickname, long dwellTime) {

            this.nickname = nickname;
            this.dwellTime = dwellTime;
        }
    }
}
