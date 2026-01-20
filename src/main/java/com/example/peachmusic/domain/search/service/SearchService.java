package com.example.peachmusic.domain.search.service;

import com.example.peachmusic.domain.album.model.response.AlbumSearchResponse;
import com.example.peachmusic.domain.album.service.AlbumService;
import com.example.peachmusic.domain.artist.model.response.ArtistSearchResponse;
import com.example.peachmusic.domain.artist.service.ArtistService;
import com.example.peachmusic.domain.search.entity.Search;
import com.example.peachmusic.domain.search.model.SearchPopularResponse;
import com.example.peachmusic.domain.search.model.SearchPreviewResponse;
import com.example.peachmusic.domain.search.repository.SearchRepository;
import com.example.peachmusic.domain.song.model.response.SongSearchResponse;
import com.example.peachmusic.domain.song.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchRepository searchRepository;
    private final ArtistService artistService;
    private final AlbumService albumService;
    private final SongService songService;

    /**
     * 통합 검색 - 미리보기
     * @param word 검색어
     * @return 검색 응답 DTO
     */
    @Transactional
    public SearchPreviewResponse searchPreview(String word) {

        List<ArtistSearchResponse> artistList = artistService.searchArtistList(word);
        List<AlbumSearchResponse> albumList = albumService.searchAlbumList(word);
        List<SongSearchResponse> songList = songService.searchSongList(word);

        recordSearch(word); // 검색어 기록

        return SearchPreviewResponse.of(word, artistList, albumList, songList);
    }

    /**
     * 날짜마다 검색어 횟수 기록
     * - 오늘 날짜에 검색어가 존재하면 검색 횟수 증가
     * - 오늘 날짜에 검색어가 존재 안 하면 검색어 저장
     * @param word 검색어
     */
    private void recordSearch(String word) {

        LocalDate dateNow = LocalDate.now(); // 오늘 날짜
        // 오늘 날짜에 키워드 검색 조회
        Optional<Search> foundSearch = searchRepository.findByWordAndSearchDate(word, dateNow);

        if (foundSearch.isPresent()) { // 존재하면 검색 횟수 증가
            foundSearch.get().increaseCount();
        } else { // 존재 안 하면 검색 저장
            Search search = new Search(word, dateNow);
            searchRepository.save(search);
        }
    }

    /**
     * 인기 검색어 조회
     * @return 인기 검색어 응답 DTO
     */
    @Transactional
    public List<SearchPopularResponse> searchPopular() {
        return searchRepository.findPopularKeyword();
    }
}
