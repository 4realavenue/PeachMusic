package com.example.peachmusic.domain.ranking.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

@Getter
@RequiredArgsConstructor
public class RankingResponseDto {

    private final String title;
    private final Long score;
    private final Long id;


    // Tuple을 사용하여 RankingResponseDto를 생성하는 메서드
    public static RankingResponseDto of(TypedTuple<String> tuple) {

        String[] values = tuple.getValue().split(":");
        String title = values[0];
        Long id =  Long.valueOf(values[1]);

        return new RankingResponseDto(title, tuple.getScore().longValue(), id);

    }
}