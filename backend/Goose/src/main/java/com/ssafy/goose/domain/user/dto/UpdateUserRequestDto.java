package com.ssafy.goose.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateUserRequestDto {
    private String newNickname;
    private String newPassword;
}
