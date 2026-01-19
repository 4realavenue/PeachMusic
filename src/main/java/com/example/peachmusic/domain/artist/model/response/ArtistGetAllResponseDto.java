package com.example.peachmusic.domain.artist.model.response;

import com.example.peachmusic.domain.artist.entity.Artist;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArtistGetAllResponseDto {

    private final Long artistId;
    private final String artistName;
    private final Long likeCount;

    public static ArtistGetAllResponseDto from(Artist artist) {
        return new ArtistGetAllResponseDto(artist.getArtistId(), artist.getArtistName(), artist.getLikeCount());
    }
}
