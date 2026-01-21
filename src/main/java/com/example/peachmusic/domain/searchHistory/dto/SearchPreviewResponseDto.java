package com.example.peachmusic.domain.searchHistory.dto;

import com.example.peachmusic.domain.album.model.response.AlbumSearchResponse;
import com.example.peachmusic.domain.artist.model.response.ArtistSearchResponse;
import com.example.peachmusic.domain.song.model.response.SongSearchResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class SearchPreviewResponseDto {

    private final String keyword;
    private final List<ArtistSearchResponse> artists;
    private final List<AlbumSearchResponse> albums;
    private final List<SongSearchResponse> songs;

    public static SearchPreviewResponseDto of(String keyword, List<ArtistSearchResponse> artists, List<AlbumSearchResponse> albums, List<SongSearchResponse> songs) {
        return new SearchPreviewResponseDto(keyword, artists, albums, songs);
    }
}
