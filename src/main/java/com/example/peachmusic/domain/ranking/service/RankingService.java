package com.example.peachmusic.domain.ranking.service;

import com.example.peachmusic.common.constants.RedisResetTime;
import com.example.peachmusic.domain.ranking.model.RankingResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.example.peachmusic.domain.song.service.SongService.MUSIC_DAILY_KEY;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RedisTemplate<String, String> redisTemplate;

    public List<RankingResponseDto> findMusicTop100() {

        LocalDate currentDate = LocalDate.now();


        // 현재일로부터 1주의 데이터를 리스트로 만들어 나열함
        List<String> keyList = List.of(
                MUSIC_DAILY_KEY + currentDate,
                MUSIC_DAILY_KEY + currentDate.minusDays(1),
                MUSIC_DAILY_KEY + currentDate.minusDays(2),
                MUSIC_DAILY_KEY + currentDate.minusDays(3),
                MUSIC_DAILY_KEY + currentDate.minusDays(4),
                MUSIC_DAILY_KEY + currentDate.minusDays(5),
                MUSIC_DAILY_KEY + currentDate.minusDays(6)
        );

        // 위 List로 값을 묶어서 music_rank:last1weeks 에 저장
        String destKey = "music_rank:last1weeks";

        // 1주간의 데이터를 병합함
        redisTemplate.opsForZSet().unionAndStore(keyList.get(0), keyList.subList(1,keyList.size()), destKey);

        // TTL 설정
        redisTemplate.expire(destKey, Duration.ofDays(RedisResetTime.RESET_DATE));

        // 1주간의 데이터를 합친것 중 상위 Top 100 뽑아냄
        Set<TypedTuple<String>> result = redisTemplate
                .opsForZSet().reverseRangeWithScores(destKey, 0, 100);

        // 결과가 빈값이면 빈리스트 반환
        if ( result == null ) {
            return Collections.emptyList();
        }

        // Set<typedTuple<String>> -> List<RankingDto> 변환
        return result.stream()
                .map(RankingResponseDto::of)
                .toList();
    }
}