package com.example.peachmusic.domain.artist.model.response;

import com.example.peachmusic.domain.artist.entity.Artist;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArtistUpdateResponseDto {

    private final Long artistId;
    private final String artistName;
    private final Long likeCount;

    public static ArtistUpdateResponseDto from(Artist artist) {
        return new ArtistUpdateResponseDto(artist.getArtistId(), artist.getArtistName(), artist.getLikeCount());
    }
}
