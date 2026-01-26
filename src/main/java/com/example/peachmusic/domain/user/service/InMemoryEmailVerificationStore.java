package com.example.peachmusic.domain.user.service;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryEmailVerificationStore {
    private static final long EXPIRATION_TIME = 30 * 60 * 1000;
    private final Map<String, String> verificationCodes = new HashMap<>();
    private final Map<String, Long> expirationTimes = new HashMap<>();


    public void saveVerificationCode(String email, String code) {
        verificationCodes.put(email, code);
        expirationTimes.put(email, System.currentTimeMillis() + EXPIRATION_TIME);
    }

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

