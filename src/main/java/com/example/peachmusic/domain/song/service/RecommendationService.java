package com.example.peachmusic.domain.song.service;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.playlistsong.repository.PlaylistSongRepository;
import com.example.peachmusic.domain.song.dto.SongFeatureDto;
import com.example.peachmusic.domain.song.dto.SongRecommendationScoreDto;
import com.example.peachmusic.domain.song.dto.response.SongRecommendationResponseDto;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.songlike.repository.SongLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final SongRepository songRepository;
    private final SongLikeRepository songLikeRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final FeatureVectorizer featureVectorizer;

    @Transactional(readOnly = true)
    public Slice<SongRecommendationResponseDto> getRecommendedSongs(AuthUser authUser, Pageable pageable) {

        List<Long> likeSongIdList = songLikeRepository.findSongsLikedByUser(authUser.getUserId());
        List<Long> playlistSongIdList = playlistSongRepository.findSongsPlaylistByUser(authUser.getUserId());

        Set<Long> seedSongIdList = new LinkedHashSet<>(likeSongIdList);
        seedSongIdList.addAll(playlistSongIdList);
        List<Long> mergedSongIdList = new ArrayList<>(seedSongIdList);

        if(mergedSongIdList.isEmpty()) {
            return new SliceImpl<>(List.of(), pageable, false);
        }

        // songId, 장르, 스피드, 태그, 악기 조회 리스트 -> User vector/User Profile 생성
        Map<Long, SongFeatureDto> seedFeatureMap = songRepository.findFeatureBySongIdList(mergedSongIdList);
        Map<String, Double> userVector = featureVectorizer.vectorizeUser(new ArrayList<>(seedFeatureMap.values()));

        Map<Long, SongFeatureDto> candidateFeatureMap = songRepository.findCandidateFeatureList(mergedSongIdList);

        List<SongRecommendationScoreDto> scoredSongList = new ArrayList<>();
        for(SongFeatureDto candidateDto : candidateFeatureMap.values()) {
            Map<String, Double> candidateVector = featureVectorizer.vectorizeSong(candidateDto);

            // 코사인 유사도 계산 (두 벡터가 이미 정규화되었으므로 단순 내적만 수행)
            double score = 0.0;
            for (String key : userVector.keySet()) {
                if (candidateVector.containsKey(key)) {
                    score += userVector.get(key) * candidateVector.get(key);
                }
            }

            if (score > 0) {
                scoredSongList.add(new SongRecommendationScoreDto(candidateDto, score));
            }
        }

        scoredSongList.sort((o1, o2) -> Double.compare(o2.getScore(), o1.getScore()));

        List<Long> orderBySongIdList = new ArrayList<>();
        for (int i = 0; i < Math.min(scoredSongList.size(), 50); i++) {
            orderBySongIdList.add(scoredSongList.get(i).getSongFeatureDto().getSongId());
        }

        Slice<SongRecommendationResponseDto> result = songRepository.getRecommendationSong(orderBySongIdList, pageable);

        return result;
    }
}
