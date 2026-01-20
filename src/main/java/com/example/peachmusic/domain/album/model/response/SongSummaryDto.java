package com.example.peachmusic.domain.album.model.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SongSummaryDto {

    private final Long songId;
    private final String name;
    private final Long duration;
    private final Long position;
}
