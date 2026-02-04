package com.example.peachmusic.domain.ranking.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

@Getter
@RequiredArgsConstructor
public class RankingResponseDto {

    private final String title;
    private final double score;

    // Tuple을 사용하여 RankingResponseDto를 생성하는 메서드
    public static RankingResponseDto of(TypedTuple<String> tuple) {
        return new RankingResponseDto(
                tuple.getValue(),
                tuple.getScore() // score는 double 타입
        );
    }
}
