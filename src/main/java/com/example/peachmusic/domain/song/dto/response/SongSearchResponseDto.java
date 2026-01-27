package com.example.peachmusic.domain.song.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SongSearchResponseDto {

    private final Long songId;
    private final String name;
    private final String artistName;
    private final Long likeCount;
    private final String albumImage;
    private final boolean isDeleted;
}
