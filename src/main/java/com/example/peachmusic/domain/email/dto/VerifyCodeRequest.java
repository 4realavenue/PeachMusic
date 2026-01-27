package com.example.peachmusic.domain.email.dto;

import jakarta.validation.constraints.NotBlank;

public class VerifyCodeRequest {

    @NotBlank(message = "이메일은 공백일 수 없습니다.")
    private String email;

    @NotBlank(message = "인증 코드는 공백일 수 없습니다.")
    private String code;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
