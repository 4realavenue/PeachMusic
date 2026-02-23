package com.example.peachmusic.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserCreateRequestDto {

    @NotBlank(message = "name을 입력해주세요.")
    private String name;

    @NotBlank(message = "nickname을 입력해주세요.")
    private String nickname;

    @Email
    @NotBlank(message = "email을 입력해주세요.")
    private String email;

    @NotBlank(message = "password를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "비밀번호는 최소 8자 이상, 대문자, 소문자, 숫자 및 특수문자를 포함해야 합니다.")
    private String password;
}