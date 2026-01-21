package com.example.peachmusic.domain.album.model.response;

import com.example.peachmusic.domain.album.entity.Album;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class AlbumGetDetailResponseDto {

    private final Long albumId;
    private final String albumName;
    private final LocalDate albumReleaseDate;
    private final String albumImage;
    private final List<ArtistSummaryDto> artistList;
    private final List<SongSummaryDto> songList;
    private final Long likeCount;

    public static AlbumGetDetailResponseDto from(Album album, List<ArtistSummaryDto> artistList, List<SongSummaryDto> songList) {
        return new AlbumGetDetailResponseDto(
                album.getAlbumId(), album.getAlbumName(), album.getAlbumReleaseDate(), album.getAlbumImage(), artistList, songList, album.getLikeCount());
    }
}
