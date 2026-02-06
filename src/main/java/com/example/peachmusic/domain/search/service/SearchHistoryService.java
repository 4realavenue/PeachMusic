package com.example.peachmusic.domain.search.service;

import com.example.peachmusic.domain.search.dto.SearchPopularResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final RedisTemplate<String, String> redisTemplate;
    private final String SEARCH_KEY = "searchWord:";

    /**
     * Redis zset 사용하여 날짜+시간마다 검색어 횟수 기록
     * - TTL을 설정하여 24시간 뒤면 삭제됨
     * @param word 검색어
     */
    public void recordSearch(String word) {

        // 매 시간마다 검색어 랭킹 기록
        String key = SEARCH_KEY + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
        if (!redisTemplate.hasKey(key)) {
            redisTemplate.opsForZSet().add(key, word, 1);
            redisTemplate.expire(key, 24, TimeUnit.HOURS); // 24시간 뒤면 삭제
        } else {
            redisTemplate.opsForZSet().incrementScore(key, word, 1);
        }
    }

    /**
     * 최근 24시간 인기 검색어 TOP 10 조회
     */
    @Transactional(readOnly = true)
    public List<SearchPopularResponseDto> searchPopular() {

        // 최근 24시간 동안의 랭킹 만들기
        String key = SEARCH_KEY + "last24hours";
        List<String> keyList = getLast24HourKeyList();
        redisTemplate.opsForZSet().unionAndStore(keyList.get(0), keyList.subList(1, keyList.size()), key);
        redisTemplate.expire(key, 3, TimeUnit.MINUTES);

        Set<TypedTuple<String>> tupleSet = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 9);

        if (tupleSet == null) {
            return Collections.emptyList();
        }

        int rank = 1;
        List<SearchPopularResponseDto> result = new ArrayList<>();
        for (TypedTuple<String> tuple : tupleSet) {
            result.add(SearchPopularResponseDto.of(rank++, tuple));
        }

        return result;
    }

    /**
     * 지난 24시간 전까지의 key 묶기
     */
    private List<String> getLast24HourKeyList() {
        List<String> keyList = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 24; i++) {
            LocalDateTime time = now.minusHours(i);
            String key = SEARCH_KEY + time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
            keyList.add(key);
        }
        return keyList;
    }

}
