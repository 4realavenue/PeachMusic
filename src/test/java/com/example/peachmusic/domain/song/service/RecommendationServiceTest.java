package com.example.peachmusic.domain.song.service;

import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.playlistsong.repository.PlaylistSongRepository;
import com.example.peachmusic.domain.song.dto.SongFeatureDto;
import com.example.peachmusic.domain.song.dto.response.SongRecommendationResponseDto;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.songlike.repository.SongLikeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private SongRepository songRepository;

    @Mock
    private SongLikeRepository songLikeRepository;

    @Mock
    private PlaylistSongRepository playlistSongRepository;

    @Mock
    private FeatureVectorizer featureVectorizer;

    @InjectMocks
    private RecommendationService recommendationService;

    private SongRecommendationResponseDto createDummyDto(Long songId) {
        return new SongRecommendationResponseDto(songId, "song-" + songId, 100L, "artist", 200L, "album", "album.jpg", 50L);
    }

    @Test
    @DisplayName("추천 조회 성공 - Seed가 없으면 Cold Start 추천을 반환")
    void success_getRecommendedSongSlice_coldStart() {
        // given 로그인한 유저 정보와 페이지 정보
        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER, 1L);
        Pageable pageable = Pageable.ofSize(10);

        // 사용자가 좋아요/플레이리스트에 아무 음원도 없을 때
        given(songLikeRepository.findSongsLikedByUser(authUser.getUserId())).willReturn(Collections.emptyList());
        given(playlistSongRepository.findSongsPlaylistByUser(authUser.getUserId())).willReturn(Collections.emptyList());

        // cold-start 추천 결과
        SongRecommendationResponseDto dto = createDummyDto(1L);
        List<SongRecommendationResponseDto> list = List.of(dto);
        given(songRepository.findRecommendedSongSliceForColdStart(pageable)).willReturn(list);

        // when
        List<SongRecommendationResponseDto> result = recommendationService.getRecommendedSongSlice(authUser, pageable);

        // then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getSongId());

        verify(songRepository).findRecommendedSongSliceForColdStart(pageable);
    }


    @Test
    @DisplayName("추천 조회 성공 - Seed가 있으면 추천 로직을 수행")
    void success_getRecommendedSongSlice_withSeed() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER, 1L);
        Pageable pageable = Pageable.ofSize(10);

        // 좋아요한 곡이 1번 곡 하나 있다고 가정
        given(songLikeRepository.findSongsLikedByUser(authUser.getUserId())).willReturn(List.of(1L));
        given(playlistSongRepository.findSongsPlaylistByUser(authUser.getUserId())).willReturn(Collections.emptyList());

        // 장르 ID
        List<Long> genreIdList = List.of(10L);

        // Seed Genre 조회 mock
        given(songRepository.findSeedGenreList(List.of(1L))).willReturn(genreIdList);

        // Seed 곡 feature 정보
        SongFeatureDto seedFeature = new SongFeatureDto(1L, List.of("Pop"), "high", null, null);
        given(songRepository.findFeatureBySongIdMap(List.of(1L))).willReturn(Map.of(1L, seedFeature));
        given(featureVectorizer.vectorizeUserMap(List.of(seedFeature))).willReturn(Map.of("g:pop", 1.0));

        // 추천 후보 곡 feature
        SongFeatureDto recommendFeature = new SongFeatureDto(2L, List.of("Pop"), "high", null, null);
        given(songRepository.findRecommendFeatureMap(List.of(1L), genreIdList)).willReturn(Map.of(2L, recommendFeature));
        given(featureVectorizer.vectorizeSongMap(recommendFeature)).willReturn(Map.of("g:pop", 1.0));

        // 최종 추천 결과
        SongRecommendationResponseDto dto = createDummyDto(2L);
        List<SongRecommendationResponseDto> list = List.of(dto);
        given(songRepository.findRecommendedSongSlice(List.of(2L), pageable)).willReturn(list);

        // when
        List<SongRecommendationResponseDto> result = recommendationService.getRecommendedSongSlice(authUser, pageable);

        // then
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getSongId());

        verify(songRepository).findSeedGenreList(List.of(1L));
        verify(songRepository).findRecommendedSongSlice(List.of(2L), pageable);
    }


    @Test
    @DisplayName("실패 - 추천 후보가 없으면 빈 추천 결과를 반환")
    void fail_getRecommendedSongs_noCandidateSongSlice() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER, 1L);
        Pageable pageable = Pageable.ofSize(10);

        given(songLikeRepository.findSongsLikedByUser(authUser.getUserId())).willReturn(List.of(1L));

        given(playlistSongRepository.findSongsPlaylistByUser(authUser.getUserId())).willReturn(Collections.emptyList());

        // 장르 ID
        List<Long> genreIdList = List.of(10L);

        // Seed Genre 조회 mock
        given(songRepository.findSeedGenreList(List.of(1L))).willReturn(genreIdList);
        SongFeatureDto seedFeature = new SongFeatureDto(1L, List.of("Pop"), "high", null, null);
        given(songRepository.findFeatureBySongIdMap(List.of(1L))).willReturn(Map.of(1L, seedFeature));
        given(featureVectorizer.vectorizeUserMap(List.of(seedFeature))).willReturn(Map.of("g:pop", 1.0));

        // 추천 후보가 없는 상황
        given(songRepository.findRecommendFeatureMap(List.of(1L), genreIdList)).willReturn(Collections.emptyMap());

        // when
        List<SongRecommendationResponseDto> result = recommendationService.getRecommendedSongSlice(authUser, pageable);

        // then
        assertTrue(result.isEmpty());
        verify(songRepository).findRecommendFeatureMap(List.of(1L), genreIdList);
    }
}
