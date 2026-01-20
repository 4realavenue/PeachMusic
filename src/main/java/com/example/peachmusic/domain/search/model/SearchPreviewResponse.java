package com.example.peachmusic.domain.search.model;

import com.example.peachmusic.domain.album.model.response.AlbumSearchResponse;
import com.example.peachmusic.domain.artist.model.response.ArtistSearchResponse;
import com.example.peachmusic.domain.song.model.response.SongSearchResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class SearchPreviewResponse {

    private final String keyword;
    private final List<ArtistSearchResponse> artists;
    private final List<AlbumSearchResponse> albums;
    private final List<SongSearchResponse> songs;

    public static SearchPreviewResponse of(String keyword, List<ArtistSearchResponse> artists, List<AlbumSearchResponse> albums, List<SongSearchResponse> songs) {
        return new SearchPreviewResponse(keyword, artists, albums, songs);
    }
}
