package com.example.peachmusic.domain.mail.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.mail.Service.MailService;
import com.example.peachmusic.domain.mail.dto.SendEmailRequestDto;
import com.example.peachmusic.domain.mail.dto.VerifyCodeRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    /**
     *  이메일 발송
     */
    @PostMapping("/auth/email/code-send")
    public ResponseEntity<CommonResponse<Void>> sendVerificationCode(@Valid @RequestBody SendEmailRequestDto request) {
        mailService.sendCodeToEmail(request.getEmail());
        return ResponseEntity.ok(CommonResponse.success("인증 코드가 발송되었습니다."));
    }

    /**
     *  코드 인증
     */
    @PostMapping("/auth/email/verify-code")
    public ResponseEntity<CommonResponse<Void>> verifyCode(@Valid @RequestBody VerifyCodeRequestDto request) {
        CommonResponse<Void> response = mailService.verifyEmailCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(response);
    }
}