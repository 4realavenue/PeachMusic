package com.example.peachmusic.domain.artistLike.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArtistLikeResponseDto {

    private final Long artistId;
    private final Boolean liked;
    private final Long likeCount;

    public static ArtistLikeResponseDto of(Long artistId, boolean liked, long likeCount) {
        return new ArtistLikeResponseDto(artistId, liked, likeCount);
    }
}
