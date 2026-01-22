package com.example.peachmusic.domain.searchHistory.service;

import com.example.peachmusic.domain.album.model.response.AlbumSearchResponse;
import com.example.peachmusic.domain.album.service.AlbumService;
import com.example.peachmusic.domain.artist.model.response.ArtistSearchResponse;
import com.example.peachmusic.domain.artist.service.ArtistService;
import com.example.peachmusic.domain.searchHistory.dto.SearchPreviewResponseDto;
import com.example.peachmusic.domain.song.model.response.SongSearchResponse;
import com.example.peachmusic.domain.song.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchHistoryService searchHistoryService;
    private final ArtistService artistService;
    private final AlbumService albumService;
    private final SongService songService;

    /**
     * 통합 검색 - 미리보기
     * @param word 검색어
     * @return 검색 응답 DTO
     */
    @Transactional(readOnly = true)
    public SearchPreviewResponseDto searchPreview(String word) {

        List<ArtistSearchResponse> artistList = artistService.searchArtistList(word);
        List<AlbumSearchResponse> albumList = albumService.searchAlbumList(word);
        List<SongSearchResponse> songList = songService.searchSongList(word);

        searchHistoryService.recordSearch(word); // 검색어 기록

        return SearchPreviewResponseDto.of(word, artistList, albumList, songList);
    }
}
