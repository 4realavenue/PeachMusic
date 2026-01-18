package com.example.peachmusic.domain.album.model.response;

import com.example.peachmusic.domain.album.entity.Album;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class AlbumCreateResponseDto {

    private final Long albumId;
    private final String albumName;
    private final LocalDate albumReleaseDate;
    private final String albumImage;
    private final List<ArtistSummaryDto> artistList;
    private final Long likeCount;

    public static AlbumCreateResponseDto from(Album album, List<ArtistSummaryDto> dtoList) {
        return new AlbumCreateResponseDto(
                album.getAlbumId(),
                album.getAlbumName(),
                album.getAlbumReleaseDate(),
                album.getAlbumImage(),
                dtoList,
                album.getLikeCount());
    }
}
