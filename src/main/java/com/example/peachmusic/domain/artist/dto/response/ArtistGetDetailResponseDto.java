package com.example.peachmusic.domain.artist.dto.response;

import com.example.peachmusic.domain.artist.entity.Artist;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ArtistGetDetailResponseDto {

    private final Long artistId;
    private final String artistName;
    private final Long likeCount;
    private final boolean liked;

    public static ArtistGetDetailResponseDto from(Artist artist, boolean liked) {
        return new ArtistGetDetailResponseDto(artist.getArtistId(), artist.getArtistName(), artist.getLikeCount(), liked);
    }
}
