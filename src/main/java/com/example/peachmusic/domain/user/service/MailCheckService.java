package com.example.peachmusic.domain.user.service;

import com.example.peachmusic.common.config.InMemoryEmailVerificationConfig;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailCheckService {

    private final MailService mailService;
    private final InMemoryEmailVerificationConfig verificationStore;

    /**
     *  코드 전송
     */
    public void sendCodeToEmail(String email) {

        String authCode = generateAuthCode();
        verificationStore.saveVerificationCode(email, authCode);

        String title = "[PeachMusic] 이메일 인증 코드";
        String text = String.format("인증 코드: %s\n이 코드는 3분간 유효합니다.", authCode);
        mailService.sendEmail(email, title, text);
    }

    /**
     *  코드 일치 여부 확인
     */
    public boolean verifyEmailCode(String email, String code) {
        String savedCode = verificationStore.getVerificationCode(email);
        if (savedCode == null) {
            throw new CustomException(ErrorCode.AUTH_CODE_EXPIRED);
        }
        if (!savedCode.equals(code)) {
            throw new CustomException(ErrorCode.AUTH_CODE_INVALID);
        }
        verificationStore.removeVerificationCode(email);
        verificationStore.verifyEmail(email);
        return true;
    }

    /**
     *  이메일 체크
     */
    public boolean isEmailVerified(String email) {
        return verificationStore.isEmailVerified(email);
    }

    /**
     *  코드 생성
     */
    private String generateAuthCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }
}