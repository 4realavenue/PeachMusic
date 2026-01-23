package com.example.peachmusic.domain.artist.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ArtistSearchResponseDto {

    private final Long artistId;
    private final String artistName;
    private final Long likeCount;
    private final boolean isDeleted;
}
