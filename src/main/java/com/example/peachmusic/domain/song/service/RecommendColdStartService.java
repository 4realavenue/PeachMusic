package com.example.peachmusic.domain.song.service;

import com.example.peachmusic.domain.song.dto.response.SongRecommendationResponseDto;
import com.example.peachmusic.domain.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RecommendColdStartService {

    private final RedisTemplate<String, List<SongRecommendationResponseDto>> coldStartRedisTemplate;
    private final SongRepository songRepository;

    private static final String COLD_START_KEY = "recommend:coldStart:top50";
    private static final long TTL_MINUTE = 30L;

    // cold-start 추천 좋아요 기준 top50 조회
    @Transactional(readOnly = true)
    public List<SongRecommendationResponseDto> getTop50() {

        List<SongRecommendationResponseDto> cached = coldStartRedisTemplate.opsForValue().get(COLD_START_KEY);

        if (cached != null) {
            return cached;
        }

        List<SongRecommendationResponseDto> result = songRepository.findRecommendedSongListForColdStart();

        coldStartRedisTemplate.opsForValue().set(COLD_START_KEY, result, TTL_MINUTE, TimeUnit.MINUTES);
        return result;
    }
}