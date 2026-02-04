package com.example.peachmusic.domain.searchhistory.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

@Getter
@RequiredArgsConstructor
public class SearchPopularResponseDto {

    private final int rank;
    private final String keyword;
    private final Long count;

    public static SearchPopularResponseDto of(int rank, TypedTuple<String> tuple) {
        return new SearchPopularResponseDto(rank, tuple.getValue(), tuple.getScore().longValue());
    }
}
