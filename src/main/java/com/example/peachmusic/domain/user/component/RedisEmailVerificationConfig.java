package com.example.peachmusic.domain.user.component;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisEmailVerificationConfig {
    private static final long EXPIRATION_TIME = 3;
    private static final TimeUnit TIME_UNIT = TimeUnit.MINUTES;
    private final RedisTemplate<String, String> redisTemplate;

    public RedisEmailVerificationConfig(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     *  인증정보 저장
     */
    public void saveVerificationCode(String email, String code) {
        redisTemplate.opsForValue().set(email + ":code", code, EXPIRATION_TIME, TIME_UNIT);
        redisTemplate.opsForValue().set(email + ":verified", "false", EXPIRATION_TIME, TIME_UNIT);
    }

    /**
     *  인증 확인
     */
    public boolean isEmailVerified(String email) {
        String verifiedStatus = redisTemplate.opsForValue().get(email + ":verified");
        return "true".equals(verifiedStatus);
    }

    /**
     *  이메일 인증
     */
    public void verifyEmail(String email) {
        redisTemplate.opsForValue().set(email + ":verified", "true", EXPIRATION_TIME, TIME_UNIT);
    }

    /**
     *  이메일 인증 코드 가져오기
     */
    public String getVerificationCode(String email) {
        String code = redisTemplate.opsForValue().get(email + ":code");
        if (isCodeExpired(email)) {
            removeVerificationCode(email);
            return null;
        }
        return code;
    }

    public void removeVerificationCode(String email) {
        redisTemplate.delete(email + ":code");
        redisTemplate.delete(email + ":verified");
    }

    public boolean isCodeExpired(String email) {
        String code = redisTemplate.opsForValue().get(email + ":code");
        return code == null;
    }
}
