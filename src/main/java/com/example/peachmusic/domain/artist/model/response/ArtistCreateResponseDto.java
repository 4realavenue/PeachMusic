package com.example.peachmusic.domain.artist.model.response;

import com.example.peachmusic.domain.artist.entity.Artist;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ArtistCreateResponseDto {

    private final Long artistId;
    private final String artistName;
    private final Long likeCount;

    public static ArtistCreateResponseDto from(Artist artist) {
        return new ArtistCreateResponseDto(artist.getArtistId(), artist.getArtistName(), artist.getLikeCount());
    }
}
