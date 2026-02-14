package com.example.peachmusic.domain.search.service;

import com.example.peachmusic.domain.album.dto.response.AlbumSearchResponseDto;
import com.example.peachmusic.domain.album.service.AlbumService;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import com.example.peachmusic.domain.artist.service.ArtistService;
import com.example.peachmusic.domain.search.dto.SearchPreviewResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
import com.example.peachmusic.domain.song.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchHistoryService searchHistoryService;
    private final ArtistService artistService;
    private final AlbumService albumService;
    private final SongService songService;

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String SEARCH_RESULT = "search:result:";

    /**
     * 통합 검색 - 미리보기
     * @param word 검색어
     * @return 검색 응답 DTO
     */
    @Transactional(readOnly = true)
    public SearchPreviewResponseDto searchPreview(String word) {

        word = word.trim();

        searchHistoryService.recordSearchRank(word); // 검색어 랭킹 기록

        boolean isPopular = searchHistoryService.isPopularKeyword(word);
        String key = SEARCH_RESULT + word;

        if (isPopular) { // 검색어가 인기검색어인지 확인
            // 검색 결과를 redis에서 찾기
            SearchPreviewResponseDto cached = (SearchPreviewResponseDto) redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return cached;
            }
        }

        List<ArtistSearchResponseDto> artistList = artistService.searchArtistList(word);
        List<AlbumSearchResponseDto> albumList = albumService.searchAlbumList(word);
        List<SongSearchResponseDto> songList = songService.searchSongList(word);
        SearchPreviewResponseDto result = SearchPreviewResponseDto.of(word, artistList, albumList, songList);

        if (isPopular) {
            redisTemplate.opsForValue().set(key, result, 5, TimeUnit.MINUTES);
        }

        return result;
    }
}
