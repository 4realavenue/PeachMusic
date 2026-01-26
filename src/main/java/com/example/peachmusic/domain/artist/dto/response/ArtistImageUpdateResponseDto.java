package com.example.peachmusic.domain.artist.dto.response;

import com.example.peachmusic.common.enums.ArtistType;
import com.example.peachmusic.domain.artist.entity.Artist;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class ArtistImageUpdateResponseDto {

    private final Long artistId;
    private final String artistName;
    private final String profileImage;
    private final String country;
    private final ArtistType artistType;
    private final LocalDate debutDate;
    private final String bio;
    private final Long likeCount;

    public static ArtistImageUpdateResponseDto from(Artist artist) {
        return new ArtistImageUpdateResponseDto(
                artist.getArtistId(), artist.getArtistName(), artist.getProfileImage(), artist.getCountry(), artist.getArtistType(),
                artist.getDebutDate(), artist.getBio(), artist.getLikeCount());
    }
}
