package com.example.peachmusic.domain.albumLike.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AlbumLikeResponseDto {

    private final Long albumId;
    private final Boolean liked;
    private final Long likeCount;

    public static AlbumLikeResponseDto of(Long albumId, boolean liked, long likeCount) {
        return new AlbumLikeResponseDto(albumId, liked, likeCount);
    }
}
