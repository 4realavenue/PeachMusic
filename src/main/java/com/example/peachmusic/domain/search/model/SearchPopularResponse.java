package com.example.peachmusic.domain.search.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SearchPopularResponse {

    private final int rank;
    private final String keyword;
    private final Long count;
}
