package com.example.peachmusic.domain.songlike.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SongLikeResponseDto {

    private final Long songId;
    private final String songName;
    private final boolean liked;
    private final Long likeCount;

    public static SongLikeResponseDto of(Long songId, String songName, boolean liked, Long likeCount) {
        return new SongLikeResponseDto(songId, songName, liked, likeCount);
    }
}
