package com.example.peachmusic.domain.artistLike.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ArtistLikeResponseDto {

    private final Long artistId;
    private final String artistName;
    private final Boolean liked;
    private final Long likeCount;

    public static ArtistLikeResponseDto of(Long artistId, String artistName, boolean liked, long likeCount) {
        return new ArtistLikeResponseDto(artistId, artistName, liked, likeCount);
    }
}
