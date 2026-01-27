package com.example.peachmusic.domain.searchhistory.dto;

import com.example.peachmusic.domain.album.dto.response.AlbumSearchResponseDto;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class SearchPreviewResponseDto {

    private final String keyword;
    private final List<ArtistSearchResponseDto> artists;
    private final List<AlbumSearchResponseDto> albums;
    private final List<SongSearchResponseDto> songs;

    public static SearchPreviewResponseDto of(String keyword, List<ArtistSearchResponseDto> artists, List<AlbumSearchResponseDto> albums, List<SongSearchResponseDto> songs) {
        return new SearchPreviewResponseDto(keyword, artists, albums, songs);
    }
}
