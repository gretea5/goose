package com.ssafy.goose.domain.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequestDto {
    private String username;
    private String nickname;
    private String password;
}
