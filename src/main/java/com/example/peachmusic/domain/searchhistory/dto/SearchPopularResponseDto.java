package com.example.peachmusic.domain.searchhistory.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SearchPopularResponseDto {

    private final int rank;
    private final String keyword;
    private final Long count;
}
