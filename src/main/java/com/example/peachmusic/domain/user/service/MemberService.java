package com.example.peachmusic.domain.user.service;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MailService mailService;
    private final InMemoryEmailVerificationStore verificationStore; // 메모리 저장소 사용

    public void sendCodeToEmail(String email) {

        String authCode = generateAuthCode();

        verificationStore.saveVerificationCode(email, authCode);

        String title = "[PeachMusic] 이메일 인증 코드";
        String text = String.format("인증 코드: %s\n이 코드는 30분간 유효합니다.", authCode);
        mailService.sendEmail(email, title, text);

        log.info("인증 코드 발송 완료 - email: {}, code: {}", email, authCode);
    }

    @Transactional
    public void verifyEmailCode(String email, String code) {

        String savedCode = verificationStore.getVerificationCode(email);
        if (savedCode == null) {
            throw new CustomException(ErrorCode.AUTH_CODE_EXPIRED);
        }

        if (!savedCode.equals(code)) {
            throw new CustomException(ErrorCode.AUTH_CODE_INVALID);
        }

        verificationStore.removeVerificationCode(email);

        log.info("이메일 인증 완료 - email: {}", email);
    }

    private String generateAuthCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
