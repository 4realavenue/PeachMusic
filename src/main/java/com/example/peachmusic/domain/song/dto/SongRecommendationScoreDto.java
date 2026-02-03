package com.example.peachmusic.domain.song.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SongRecommendationScoreDto {
    private final SongFeatureDto songFeatureDto;
    private final double score;
}
