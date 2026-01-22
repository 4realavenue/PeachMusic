package com.example.peachmusic.domain.searchHistory.dto;

import com.example.peachmusic.domain.album.dto.response.AlbumSearchResponseDto;
import com.example.peachmusic.domain.artist.model.response.ArtistSearchResponseDto;
import com.example.peachmusic.domain.song.model.response.SongSearchResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class SearchPreviewResponseDto {

    private final String keyword;
    private final List<ArtistSearchResponseDto> artists;
    private final List<AlbumSearchResponseDto> albums;
    private final List<SongSearchResponse> songs;

    public static SearchPreviewResponseDto of(String keyword, List<ArtistSearchResponseDto> artists, List<AlbumSearchResponseDto> albums, List<SongSearchResponse> songs) {
        return new SearchPreviewResponseDto(keyword, artists, albums, songs);
    }
}
