package com.example.peachmusic.domain.album.model.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ArtistSummaryDto {

    private final Long artistId;
    private final String artistName;
}
