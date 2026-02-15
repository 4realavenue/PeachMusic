package com.example.peachmusic.domain.search.service;

import com.example.peachmusic.domain.search.dto.SearchPopularResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
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
    private static final int TOP_RANK_LIMIT = 10;
    private static final String SEARCH_RANK_KEY = "search:rank:";

    /**
     * 검색어 랭킹 기록
     * - Redis zset 사용하여 날짜+시간마다 검색어 횟수 기록
     * - TTL을 설정하여 24시간 뒤면 삭제됨
     * @param word 검색어
     */
    public void recordSearchRank(String word) {

        // 매 시간마다 검색어 랭킹 기록
        String key = SEARCH_RANK_KEY + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));

        if (!redisTemplate.hasKey(key)) {
            redisTemplate.opsForZSet().add(key, word, 1);
            redisTemplate.expire(key, 24, TimeUnit.HOURS); // 24시간 뒤면 삭제
        } else {
            redisTemplate.opsForZSet().incrementScore(key, word, 1);
        }
    }

    /**
     * 검색어가 인기 검색어 TOP 10에 저장되어 있는지 여부
     */
    public boolean isPopularKeyword(String word) {

        String key = SEARCH_RANK_KEY + "last24hours";
        Long rank = redisTemplate.opsForZSet().reverseRank(key, word);

        return rank != null && rank < TOP_RANK_LIMIT;
    }

    /**
     * 최근 24시간 인기 검색어 TOP 10 조회
     */
    public List<SearchPopularResponseDto> searchPopular() {

        String key = SEARCH_RANK_KEY + "last24hours";
        Set<TypedTuple<String>> tupleSet = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, TOP_RANK_LIMIT-1);

        if (tupleSet == null || tupleSet.isEmpty()) {
            return Collections.emptyList();
        }

        int rank = 1;
        List<SearchPopularResponseDto> result = new ArrayList<>();
        for (TypedTuple<String> tuple : tupleSet) {
            result.add(SearchPopularResponseDto.of(rank++, tuple));
        }

        return result;
    }

    @Scheduled(fixedRate = 300000) // 5분마다 스케줄러 실행
    public void updateLast24HourSearchRank() {

        String key = SEARCH_RANK_KEY + "last24hours";

        List<String> keyList = getLast24HourSearchRankKeyList();

        redisTemplate.opsForZSet().unionAndStore(keyList.get(0), keyList.subList(1, keyList.size()), key);
        redisTemplate.expire(key, 6, TimeUnit.MINUTES);
    }

    /**
     * 지난 24시간 전까지의 key 묶기
     */
    private List<String> getLast24HourSearchRankKeyList() {
        List<String> keyList = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 24; i++) {
            LocalDateTime time = now.minusHours(i);
            String key = SEARCH_RANK_KEY + time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
            keyList.add(key);
        }
        return keyList;
    }

}
