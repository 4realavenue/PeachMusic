package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.domain.song.dto.SongFeatureDto;
import com.example.peachmusic.domain.song.dto.response.SongRecommendationResponseDto;
import java.util.List;
import java.util.Map;

public interface RecommendationRepository {
    Map<Long, SongFeatureDto> findFeatureBySongIdMap(List<Long> songIdList);
    Map<Long, SongFeatureDto> findRecommendFeatureMap(List<Long> songIdList, List<Long> genreIdList);
    List<SongRecommendationResponseDto> findRecommendedSongList(List<Long> orderBySongIdList);
    List<SongRecommendationResponseDto> findRecommendedSongListForColdStart();
    List<Long> findSeedGenreList(List<Long> mergedSongIdList);
}
