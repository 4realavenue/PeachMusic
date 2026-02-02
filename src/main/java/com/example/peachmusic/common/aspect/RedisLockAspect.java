package com.example.peachmusic.common.aspect;

import com.example.peachmusic.common.annotation.RedisLock;
import com.example.peachmusic.common.redis.RedisLockService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class RedisLockAspect {

    private final RedisLockService lockService;


    @Around("@annotation(redisLock)")
    public Object run(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {

        String keyPreFix = redisLock.key();

        // UUID를 기준으로 랜덤값 생성해서 소유자 증명
        String value = UUID.randomUUID().toString();
        Object[] args = joinPoint.getArgs();
        Object arg = args[0];
        String key = keyPreFix + ":" + arg;

        // 락 획득 실패시 예외 처리하고 끝내지 말고 동시에 들어온게 3개가 있다면 차례대로 락을 획들할때까지 돌려주는 로직
        while (!lockService.tryLock(key, value, redisLock.timeout())) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // locked가 True -> 락을 획득한 경우에 실행
        try {
            return joinPoint.proceed();
        } finally {
            lockService.unlock(key, value);
        }
    }
}

