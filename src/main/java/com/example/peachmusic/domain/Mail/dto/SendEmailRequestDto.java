package com.example.peachmusic.domain.Mail.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SendEmailRequestDto {

    @NotBlank(message = "이메일은 공백일 수 없습니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

}