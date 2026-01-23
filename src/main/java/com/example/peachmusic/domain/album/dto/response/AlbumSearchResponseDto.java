package com.example.peachmusic.domain.album.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class AlbumSearchResponseDto {

    private final Long albumId;
    private final String albumName;
    private final String artistName;
    private final LocalDate albumReleaseDate;
    private final String albumImage;
    private final Long likeCount;
}
