package com.example.peachmusic.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserCreateRequestDto {

    @NotBlank(message = "name을 입력해주세요.")
    private String name;

    @NotBlank(message = "nickname을 입력해주세요.")
    private String nickname;

    @Email
    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "이메일 형식이 올바르지 않습니다."
    )

    @NotBlank(message = "email을 입력해주세요.")
    private String email;

    @NotBlank(message = "password를 입력해주세요.")
    private String password;
}
