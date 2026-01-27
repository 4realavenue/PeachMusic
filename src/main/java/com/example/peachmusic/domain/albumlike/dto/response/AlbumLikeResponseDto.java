package com.example.peachmusic.domain.albumlike.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AlbumLikeResponseDto {

    private final Long albumId;
    private final String albumName;
    private final Boolean liked;
    private final Long likeCount;

    public static AlbumLikeResponseDto of(Long albumId, String albumName, boolean liked, long likeCount) {
        return new AlbumLikeResponseDto(albumId, albumName, liked, likeCount);
    }
}
