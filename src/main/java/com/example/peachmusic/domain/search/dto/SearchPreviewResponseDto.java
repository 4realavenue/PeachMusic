package com.example.peachmusic.domain.search.dto;

import com.example.peachmusic.domain.album.dto.response.AlbumSearchResponseDto;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
import java.util.List;

public record SearchPreviewResponseDto(String keyword, List<ArtistSearchResponseDto> artists, List<AlbumSearchResponseDto> albums, List<SongSearchResponseDto> songs) {

    public static SearchPreviewResponseDto of(String keyword, List<ArtistSearchResponseDto> artists, List<AlbumSearchResponseDto> albums, List<SongSearchResponseDto> songs) {
        return new SearchPreviewResponseDto(keyword, artists, albums, songs);
    }
}
