package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.domain.song.dto.SongFeatureDto;
import com.example.peachmusic.domain.song.dto.response.SongRecommendationResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import java.util.List;
import java.util.Map;

public interface RecommendationRepository {
    Map<Long, SongFeatureDto> findFeatureBySongIdMap(List<Long> songIdList);
    Map<Long, SongFeatureDto> findRecommendFeatureMap(List<Long> songIdList, List<Long> genreIdList);
    Slice<SongRecommendationResponseDto> findRecommendedSongSlice(List<Long> orderBySongIdList, Pageable pageable);
    Slice<SongRecommendationResponseDto> findRecommendedSongSliceForColdStart(Pageable pageable);
    List<Long> findSeedGenreList(List<Long> mergedSongIdList);
}
