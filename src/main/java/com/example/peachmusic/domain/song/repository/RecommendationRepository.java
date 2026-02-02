package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.domain.song.dto.SongFeatureDto;
import com.example.peachmusic.domain.song.dto.response.SongRecommendationResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import java.util.List;
import java.util.Map;

public interface RecommendationRepository {
    Map<Long, SongFeatureDto> findFeatureBySongIdList(List<Long> songIdList);
    Map<Long, SongFeatureDto> findRecommendFeatureList(List<Long> songIdList);
    Slice<SongRecommendationResponseDto> findRecommendedSongList(List<Long> orderBySongIdList, Pageable pageable);
    Slice<SongRecommendationResponseDto> findRecommendedSongsForColdStart(Pageable pageable);
}
