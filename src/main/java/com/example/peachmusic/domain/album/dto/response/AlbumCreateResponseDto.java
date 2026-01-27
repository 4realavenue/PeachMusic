package com.example.peachmusic.domain.album.dto.response;

import com.example.peachmusic.domain.album.entity.Album;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class AlbumCreateResponseDto {

    private final Long albumId;
    private final String albumName;
    private final LocalDate albumReleaseDate;
    private final String albumImage;
    private final List<ArtistSummaryDto> artistList;

    public static AlbumCreateResponseDto from(Album album, List<ArtistSummaryDto> dtoList) {
        return new AlbumCreateResponseDto(
                album.getAlbumId(), album.getAlbumName(), album.getAlbumReleaseDate(), album.getAlbumImage(), dtoList);
    }
}
