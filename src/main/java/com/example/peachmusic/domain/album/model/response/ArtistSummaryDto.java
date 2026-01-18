package com.example.peachmusic.domain.album.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArtistSummaryDto {

    private final Long artistId;
    private final String artistName;
}
