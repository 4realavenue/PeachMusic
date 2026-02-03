package com.example.peachmusic.domain.song.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class SongFeatureDto {
    private final Long songId;
    private final List<String> genreNameList;
    private final String speed;
    private final String vartags;
    private final String instruments;
}