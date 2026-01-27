package com.example.peachmusic.domain.user.controller;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.user.dto.request.SendEmailRequestDto;
import com.example.peachmusic.domain.user.dto.request.VerifyCodeRequestDto;
import com.example.peachmusic.domain.user.service.MailCheckService;
import com.example.peachmusic.domain.user.service.MailService;
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
    private final MailCheckService mailCheckService;

    /**
     *  이메일 발송
     */
    @PostMapping("/auth/email/code-send")
    public ResponseEntity<CommonResponse<Void>> sendVerificationCode(@Valid @RequestBody SendEmailRequestDto request) {
        mailCheckService.sendCodeToEmail(request.getEmail());
        return ResponseEntity.ok(CommonResponse.success("인증 코드가 발송되었습니다."));
    }

    /**
     *  코드 인증
     */
    @PostMapping("/auth/email/verify-code")
    public ResponseEntity<CommonResponse<Void>> verifyCode(@Valid @RequestBody VerifyCodeRequestDto request) {
        boolean isVerified;
        try {
            isVerified = mailCheckService.verifyEmailCode(request.getEmail(), request.getCode());
        } catch (CustomException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus())
                    .body(CommonResponse.fail(e.getMessage()));
        }

        return ResponseEntity.ok(CommonResponse.success("이메일 인증이 완료되었습니다."));
    }
}
