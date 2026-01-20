package com.example.peachmusic.domain.album.model.response;

import com.example.peachmusic.domain.album.entity.Album;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ArtistAlbumUpdateResponseDto {

    private final Long albumId;
    private final String albumName;
    private final LocalDate albumReleaseDate;
    private final String albumImage;
    private final List<ArtistSummaryDto> artistList;
    private final Long likeCount;

    public static ArtistAlbumUpdateResponseDto from(Album album, List<ArtistSummaryDto> dtoList) {
        return new ArtistAlbumUpdateResponseDto(
                album.getAlbumId(), album.getAlbumName(), album.getAlbumReleaseDate(), album.getAlbumImage(), dtoList, album.getLikeCount());
    }
}
