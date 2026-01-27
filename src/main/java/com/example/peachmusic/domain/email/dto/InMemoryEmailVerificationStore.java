package com.example.peachmusic.domain.email.dto;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryEmailVerificationStore {
    private static final long EXPIRATION_TIME = 3 * 60 * 1000;
    private final Map<String, String> verificationCodes = new HashMap<>();
    private final Map<String, Long> expirationTimes = new HashMap<>();
    private final Map<String, Boolean> emailVerificationStatus = new HashMap<>();

    /**
     *  인증정보 저장
     */
    public void saveVerificationCode(String email, String code) {
        verificationCodes.put(email, code);
        expirationTimes.put(email, System.currentTimeMillis() + EXPIRATION_TIME);
        emailVerificationStatus.put(email, false); // 인증 상태 초기화
    }

    /**
     *  인증 실패
     */
    public boolean isEmailVerified(String email) {
        return emailVerificationStatus.getOrDefault(email, false);
    }

    /**
     *  이메일 인증
     */
    public void verifyEmail(String email) {
        emailVerificationStatus.put(email, true); // 이메일 인증 처리
    }

    /**
     *  이메일
     */
    public String getVerificationCode(String email) {
        if (isCodeExpired(email)) {
            removeVerificationCode(email);
            return null;
        }
        return verificationCodes.get(email);
    }

    public void removeVerificationCode(String email) {
        verificationCodes.remove(email);
        expirationTimes.remove(email);
    }

    public boolean isCodeExpired(String email) {
        Long expirationTime = expirationTimes.get(email);
        return expirationTime == null || System.currentTimeMillis() > expirationTime;
    }
}
