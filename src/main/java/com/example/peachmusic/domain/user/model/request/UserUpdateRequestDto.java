package com.example.peachmusic.domain.user.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserUpdateRequestDto {

    @NotBlank(message = "name을 입력해주세요.")
    private String name;

    @NotBlank(message = "nickname을 입력해주세요.")
    private String nickname;



}
