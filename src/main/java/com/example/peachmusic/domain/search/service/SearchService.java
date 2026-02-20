package com.example.peachmusic.domain.search.service;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.album.dto.response.AlbumSearchResponseDto;
import com.example.peachmusic.domain.album.service.AlbumService;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import com.example.peachmusic.domain.artist.service.ArtistService;
import com.example.peachmusic.domain.search.dto.SearchPreviewResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
import com.example.peachmusic.domain.song.service.SongService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String SEARCH_RESULT_KEY = "search:result:";

    /**
     * 통합 검색 - 미리보기
     * @param word 검색어
     * @return 검색 응답 DTO
     */
    @Transactional(readOnly = true)
    public SearchPreviewResponseDto searchPreview(AuthUser authUser, String word) {

        word = word.trim();
        searchHistoryService.recordSearchRank(word); // 검색어 랭킹 기록

        String key = SEARCH_RESULT_KEY + word;
        SearchPreviewResponseDto cachedResult = getCachedResult(key);
        if (cachedResult != null) {
            return cachedResult;
        }

        SearchPreviewResponseDto result = createSearchResult(authUser, word); // DB 조회

        boolean isPopular = searchHistoryService.isPopularKeyword(word);
        if (isPopular) { // 검색어가 인기검색어인 경우에 Redis에 저장
            saveCacheResult(key, result);
        }

        return result;
    }

    /**
     * Redis에서 검색 결과 조회
     */
    private SearchPreviewResponseDto getCachedResult(String key) {
        try {
            String cached = redisTemplate.opsForValue().get(key);
            return cached == null ? null :objectMapper.readValue(cached, SearchPreviewResponseDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 조회 실패: ", e);
        }
    }


    /**
     * DB에서 검색 결과 조회
     */
    private SearchPreviewResponseDto createSearchResult(AuthUser authUser, String word) {
        List<ArtistSearchResponseDto> artistList = artistService.searchArtistList(authUser, word);
        List<AlbumSearchResponseDto> albumList = albumService.searchAlbumList(authUser, word);
        List<SongSearchResponseDto> songList = songService.searchSongList(authUser, word);

        return SearchPreviewResponseDto.of(word, artistList, albumList, songList);
    }

    /**
     * Redis에 검색 결과 저장
     */
    private void saveCacheResult(String key, SearchPreviewResponseDto result) {
        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(key, json, 5, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 저장 실패: ", e);
        }
    }
}
