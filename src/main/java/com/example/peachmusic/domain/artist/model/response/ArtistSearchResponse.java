package com.example.peachmusic.domain.artist.model.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ArtistSearchResponse {

    private final Long artistId;
    private final String artistName;
    private final Long likeCount;
}
