package com.example.peachmusic.domain.album.model.response;

import com.example.peachmusic.domain.album.entity.Album;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class AlbumGetAllResponseDto {

    private final Long albumId;
    private final String albumName;
    private final LocalDate albumReleaseDate;
    private final String albumImage;
    private final Long likeCount;

    public static AlbumGetAllResponseDto from(Album album) {
        return new AlbumGetAllResponseDto(
                album.getAlbumId(),
                album.getAlbumName(),
                album.getAlbumReleaseDate(),
                album.getAlbumImage(),
                album.getLikeCount());
    }
}
