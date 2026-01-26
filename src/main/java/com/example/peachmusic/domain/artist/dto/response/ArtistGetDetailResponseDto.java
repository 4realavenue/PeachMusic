package com.example.peachmusic.domain.artist.dto.response;

import com.example.peachmusic.common.enums.ArtistType;
import com.example.peachmusic.domain.artist.entity.Artist;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class ArtistGetDetailResponseDto {

    private final Long artistId;
    private final String artistName;
    private final String profileImage;
    private final String country;
    private final ArtistType artistType;
    private final LocalDate debutDate;
    private final String bio;
    private final Long likeCount;
    private final boolean isLiked;

    public static ArtistGetDetailResponseDto from(Artist artist, boolean isLiked) {
        return new ArtistGetDetailResponseDto(
                artist.getArtistId(), artist.getArtistName(), artist.getProfileImage(), artist.getCountry(), artist.getArtistType(),
                artist.getDebutDate(), artist.getBio(), artist.getLikeCount(), isLiked);
    }
}
