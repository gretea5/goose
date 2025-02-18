package com.ssafy.goose.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // ✅ null 값은 JSON 응답에서 제외
public class UserResponseDto {
    private boolean success;
    private String accessToken;
    private String refreshToken;
    private String error;
    private String nickname;

    // 로그인 성공 응답 (accessToken + refreshToken + nickname)
    public static UserResponseDto success(String accessToken, String refreshToken, String nickname) {
        return new UserResponseDto(true, accessToken, refreshToken, null, nickname);
    }

    // 회원가입, 로그아웃 성공 응답 (토큰 필요 없음, nickname 반환)
    public static UserResponseDto success(String nickname) {
        return new UserResponseDto(true, null, null, null, nickname);
    }

    public static UserResponseDto error(String errorMessage) {
        return new UserResponseDto(false, null, null, errorMessage, null);
    }
}
