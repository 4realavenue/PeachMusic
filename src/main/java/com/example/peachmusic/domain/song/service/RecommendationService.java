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

    /**
     * 음원 추천 기능 메인 메서드
     */
    @Transactional(readOnly = true)
    public Slice<SongRecommendationResponseDto> getRecommendedSongs(AuthUser authUser, Pageable pageable) {
        // 사용자의 좋아요 / 플레이리스트 음원 ID 병합
        List<Long> mergedSongIdList = mergeLikeAndPlaylistSongIdList(authUser);

        // 신규 회원이거나 Seed 데이터가 없으면 추천 불가능
        if(mergedSongIdList.isEmpty()) {
            return new SliceImpl<>(List.of(), pageable, false);
        }

        // songId, 장르, 스피드, 태그, 악기 등 음원 기반 User Profile Vector 생성
        Map<String, Double> userVector = createUserVector(mergedSongIdList);

        // 후보곡과 User Vector 간 유사도 계산
        List<SongRecommendationScoreDto> scoredSongList = rankCandidateSongList(mergedSongIdList, userVector);

        // 점수 기준 상위 50곡 ID 추출
        List<Long> orderBySongIdList = getTopSongIdList(scoredSongList);

        // 추천 결과 DB 조회
        return songRepository.getRecommendationSong(orderBySongIdList, pageable);
    }

    /**
     * Seed 음원 병합
     */
    private List<Long> mergeLikeAndPlaylistSongIdList(AuthUser authUser) {
        // 좋아요한 음원 목록 조회
        List<Long> likeSongIdList = songLikeRepository.findSongsLikedByUser(authUser.getUserId());
        // 플레이리스트 음원 목록 조회
        List<Long> playlistSongIdList = playlistSongRepository.findSongsPlaylistByUser(authUser.getUserId());

        // 중복 제거
        Set<Long> seedSongIdList = new LinkedHashSet<>(likeSongIdList);
        seedSongIdList.addAll(playlistSongIdList);
        return new ArrayList<>(seedSongIdList);
    }

    /**
     * User Vector 생성
     */
    private Map<String, Double> createUserVector(List<Long> mergedSongIdList) {
        // Seed 음원의 Feature 정보 조회
        Map<Long, SongFeatureDto> seedFeatureMap = songRepository.findFeatureBySongIdList(mergedSongIdList);

        // FeatureVectorizer를 통해서 User Vector 생성
        return featureVectorizer.vectorizeUser(new ArrayList<>(seedFeatureMap.values()));
    }

    /**
     * 후보곡 랭킹 계산
     */
    private List<SongRecommendationScoreDto> rankCandidateSongList(List<Long> mergedSongIdList, Map<String, Double> userVector) {
        // seed에 포함되지 않은 후보곡 Feature 조회
        Map<Long, SongFeatureDto> candidateFeatureMap = songRepository.findCandidateFeatureList(mergedSongIdList);

        List<SongRecommendationScoreDto> scoredSongList = new ArrayList<>();

        for(SongFeatureDto candidateDto : candidateFeatureMap.values()) {
            // 후보곡 Feature -> Vector 변환
            Map<String, Double> candidateVector = featureVectorizer.vectorizeSong(candidateDto);

            // 코사인 유사도 계산 (두 벡터가 이미 정규화되었으므로 단순 내적만 수행)
            double score = CosineSimilarity.compute(userVector, candidateVector);

            // 점수가 0 초과인 후보곡만 저장
            if (score > 0) {
                scoredSongList.add(new SongRecommendationScoreDto(candidateDto, score));
            }
        }
        return scoredSongList;
    }

    /**
     * 추천 상위 50곡 추출
     */
    private static List<Long> getTopSongIdList(List<SongRecommendationScoreDto> scoredSongList) {
        // 유사도 기준 내림차순 정렬
        scoredSongList.sort((o1, o2) -> Double.compare(o2.getScore(), o1.getScore()));

        List<Long> orderBySongIdList = new ArrayList<>();

        // 최대 50곡까지만 추출
        for (int i = 0; i < Math.min(scoredSongList.size(), 50); i++) {
            orderBySongIdList.add(scoredSongList.get(i).getSongFeatureDto().getSongId());
        }
        return orderBySongIdList;
    }
}