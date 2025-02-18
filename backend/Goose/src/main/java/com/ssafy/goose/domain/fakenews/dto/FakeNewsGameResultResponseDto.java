package com.ssafy.goose.domain.fakenews.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter
public class FakeNewsGameResultResponseDto {
    private Long id;
    private String nickname;
    private String newsId;
    private String userChoice;
    private Boolean correct;
    private Long dwellTime;
    private LocalDateTime solvedAt;
}
