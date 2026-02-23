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

        // 락 획득 실패 시 대기하는 로직
        while (true) {
            if (lockService.tryLock(key, value, redisLock.timeout())) {
                break; // 락을 획득하면 루프를 탈출
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // interrupt로 작업중단 OS에서 interrupt를 걸면 작동
                throw new RuntimeException("락 획득 대기중에 중단되었습니다.", e);
            }
        }

        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            throw throwable;
        } finally {
            lockService.unlock(key, value); // 락 해제
        }
    }
}