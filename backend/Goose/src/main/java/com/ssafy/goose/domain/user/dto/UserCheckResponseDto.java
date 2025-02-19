package com.ssafy.goose.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserCheckResponseDto {
    private boolean available;
    private String message;
}
