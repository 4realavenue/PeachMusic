package com.example.peachmusic.domain.song.service;

import com.example.peachmusic.domain.song.dto.response.RankingResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import static com.example.peachmusic.domain.song.service.SongService.MUSIC_DAILY_KEY;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String SONG_RANK_KEY = "music_rank:last1weeks";

    /**
     * 인기차트 TOP 100
     */
    public List<RankingResponseDto> findMusicTop100() {

        // 1주간의 데이터를 합친것 중 상위 Top 100 뽑아냄
        Set<TypedTuple<String>> result = redisTemplate.opsForZSet().reverseRangeWithScores(SONG_RANK_KEY, 0, 100);

        // 결과가 빈값이면 빈리스트 반환
        if ( result == null ) {
            return Collections.emptyList();
        }

        // Set<typedTuple<String>> -> List<RankingDto> 변환
        return result.stream().map(RankingResponseDto::of).toList();
    }

    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.MINUTES) // 1시간마다 스케줄러 실행
    public void updateLast7daysSongRank() {

        List<String> keyList = getLast7daysSongRankKeyList();

        redisTemplate.opsForZSet().unionAndStore(keyList.get(0), keyList.subList(1, keyList.size()), SONG_RANK_KEY);
        redisTemplate.expire(SONG_RANK_KEY, 65, TimeUnit.MINUTES);
    }

    /**
     * 현재일로부터 1주의 데이터를 리스트로 만들어 나열
     */
    private List<String> getLast7daysSongRankKeyList() {

        List<String> keyList = new ArrayList<>();

        LocalDate currentDate = LocalDate.now();

        for (int i = 0; i < 7; i++) {
            String key = MUSIC_DAILY_KEY + currentDate.minusDays(i);
            keyList.add(key);
        }
        return keyList;
    }
}