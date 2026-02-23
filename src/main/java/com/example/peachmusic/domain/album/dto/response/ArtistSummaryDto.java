package com.example.peachmusic.domain.album.dto.response;

import com.example.peachmusic.domain.artist.entity.Artist;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ArtistSummaryDto {

    private final Long artistId;
    private final String artistName;

    public static ArtistSummaryDto from(Artist artist) {
        return new ArtistSummaryDto(artist.getArtistId(), artist.getArtistName());
    }
}
