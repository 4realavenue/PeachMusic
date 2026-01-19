package com.example.peachmusic.domain.album.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SongSummaryDto {

    private final Long songId;
    private final String name;
    private final Long duration;
    private final Long position;
}
