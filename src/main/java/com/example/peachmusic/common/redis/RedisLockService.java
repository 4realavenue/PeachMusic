package com.example.peachmusic.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisLockService {

    private final RedisTemplate<String, Object> redisTemplate;
    // RedisTemplate<String, String>

    // Lock 획득
    public boolean tryLock(String key, Object value, long timeOutSecond) {
        // 락 획득 가능 True
        // 락 획득 불가능 False

        // key 체크
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, value, Duration.ofSeconds(timeOutSecond));

        // key 있으면 False , key 없으면 True -> Lock 획득
        return Boolean.TRUE.equals(result);
    }

    // Lock 회수
    public void unlock(String key, Object value) {

        String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "   return redis.call('del', KEYS[1]) " +
                        "else " +
                        "   return 0 " +
                        "end";

        redisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class),
                Collections.singletonList(key),
                value
        );
//        log.info("획득한 락 키 반납 :::: {} UUID == {}", Thread.currentThread().getName(), value);
    }


}
