package com.example.peachmusic.domain.searchHistory.service;

import com.example.peachmusic.domain.album.model.response.AlbumSearchResponse;
import com.example.peachmusic.domain.album.service.AlbumService;
import com.example.peachmusic.domain.artist.model.response.ArtistSearchResponse;
import com.example.peachmusic.domain.artist.service.ArtistService;
import com.example.peachmusic.domain.searchHistory.entity.SearchHistory;
import com.example.peachmusic.domain.searchHistory.dto.SearchPopularResponseDto;
import com.example.peachmusic.domain.searchHistory.dto.SearchPreviewResponseDto;
import com.example.peachmusic.domain.searchHistory.repository.SearchHistoryRepository;
import com.example.peachmusic.domain.song.model.response.SongSearchResponse;
import com.example.peachmusic.domain.song.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final SearchHistoryRepository historyRepository;
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

        recordSearch(word); // 검색어 기록

        return SearchPreviewResponseDto.of(word, artistList, albumList, songList);
    }

    /**
     * 날짜마다 검색어 횟수 기록
     * - 오늘 날짜에 검색어가 존재하면 검색 횟수 증가
     * - 오늘 날짜에 검색어가 존재 안 하면 검색어 저장
     * @param word 검색어
     */
    private void recordSearch(String word) {

        LocalDate today = LocalDate.now(); // 오늘 날짜

        historyRepository.findByWordAndSearchDate(word, today) // 오늘 날짜에 키워드 검색 조회
                .ifPresentOrElse(
                        SearchHistory::increaseCount, // 존재하면 검색 횟수 증가
                        () -> historyRepository.save(new SearchHistory(word, today)) // 존재 안 하면 검색 저장
                );
    }

    /**
     * 인기 검색어 조회
     * @return 인기 검색어 응답 DTO
     */
    @Transactional(readOnly = true)
    public List<SearchPopularResponseDto> searchPopular() {
        return historyRepository.findPopularKeyword();
    }
}
