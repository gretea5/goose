package com.ssafy.goose.domain.game.statistics.dto;

import com.ssafy.goose.domain.game.statistics.entity.SolvingTimeRanking;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class RankingResponseDto {
    private List<RankingDetailDto> top3Rankings;
    private Long userRank;

    @Getter
    @Builder
    public static class RankingDetailDto {
        private String nickname;
        private long solvingTimeSeconds;

        public static RankingDetailDto from(SolvingTimeRanking ranking) {
            return RankingDetailDto.builder()
                    .nickname(ranking.getNickname())
                    .solvingTimeSeconds(ranking.getSolvingTime().getSeconds())
                    .build();
        }
    }
}
