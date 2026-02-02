package com.example.peachmusic.common.aspect;

import com.example.peachmusic.common.annotation.RedisLock;
import com.example.peachmusic.common.redis.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisLockAspect {

    private final RedisLockService lockService;


    @Around("@annotation(redisLock)")
    public Object run(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {

        String keyPreFix = redisLock.key();

        // 누가 키를 만들었는지 소유자 증명하는 value
        // UUID를 기준으로 랜덤값 생성해서 소유자 증명
        String value = UUID.randomUUID().toString();

        Object[] args = joinPoint.getArgs();

        // RedisLock 어노테이션 어노테이션이 걸린 메서드의 첫번째 파라미터를 가져오겠다.
        Object arg = args[0];
        String key = keyPreFix + ":" + arg;

        // key = lock:account:1
//        boolean locked = lockService.tryLock(key, value, redisLock.timeout());


        // locked 라는 값이 False라면 키가 이미 있는것
        // locked 라는 값이 True라면 키가 없는것

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
//            log.info("락획득 성공 : {}", Thread.currentThread().getName());
            return joinPoint.proceed();
        } finally {
            lockService.unlock(key, value);
        }
    }
}

