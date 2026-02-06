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
    public List<SongRecommendationResponseDto> getRecommendedSongSlice(AuthUser authUser, Pageable pageable) {
        // 사용자의 좋아요 / 플레이리스트 음원 ID 병합
        List<Long> mergedSongIdList = mergeLikeAndPlaylistSongIdList(authUser.getUserId());

        // 사용자의 좋아요 / 플레이리스트 음원의 장르 아이디 조회
        List<Long> seedGenreIdList = songRepository.findSeedGenreList(mergedSongIdList);

        // 신규 회원이거나 Seed 데이터가 없으면 추천 likeCount가 높은 음원부터 50건 반환
        if (mergedSongIdList.isEmpty() || seedGenreIdList.isEmpty()) {
            return songRepository.findRecommendedSongListForColdStart(pageable);
        }

        // songId, 장르, 스피드, 태그, 악기 등 음원 기반 User Profile Vector 생성
        Map<String, Double> userVectorMap = createUserVector(mergedSongIdList);

        // 후보곡과 User Vector 간 유사도 계산
        List<SongRecommendationScoreDto> scoredSongList = rankRecommendSongList(mergedSongIdList, userVectorMap, seedGenreIdList);

        // 점수 기준 상위 50곡 ID 추출
        List<Long> orderBySongIdList = getTopSongIdList(scoredSongList);

        // 추천 결과 DB 조회
        return songRepository.findRecommendedSongList(orderBySongIdList, pageable);
    }

    /**
     * Seed 음원 병합
     */
    private List<Long> mergeLikeAndPlaylistSongIdList(Long userId) {
        // 좋아요한 음원 목록 조회
        List<Long> likeSongIdList = songLikeRepository.findSongsLikedByUser(userId);
        // 플레이리스트 음원 목록 조회
        List<Long> playlistSongIdList = playlistSongRepository.findSongsPlaylistByUser(userId);

        // 중복 제거
        Set<Long> seedSongIdSet = new LinkedHashSet<>(likeSongIdList);
        seedSongIdSet.addAll(playlistSongIdList);
        return new ArrayList<>(seedSongIdSet);
    }

    /**
     * User Vector 생성
     */
    private Map<String, Double> createUserVector(List<Long> mergedSongIdList) {
        // Seed 음원의 Feature 정보 조회
        Map<Long, SongFeatureDto> seedFeatureMap = songRepository.findFeatureBySongIdMap(mergedSongIdList);

        // FeatureVectorizer를 통해서 User Vector 생성
        return featureVectorizer.vectorizeUserMap(new ArrayList<>(seedFeatureMap.values()));
    }

    /**
     * 후보곡 랭킹 계산
     */
    private List<SongRecommendationScoreDto> rankRecommendSongList(List<Long> mergedSongIdList, Map<String, Double> userVector, List<Long> genreIdList) {
        // seed에 포함되지 않은 후보곡 Feature 조회
        Map<Long, SongFeatureDto> recommendFeatureMap = songRepository.findRecommendFeatureMap(mergedSongIdList, genreIdList);

        List<SongRecommendationScoreDto> scoredSongList = new ArrayList<>();

        for (SongFeatureDto songFeatureDto : recommendFeatureMap.values()) {
            // 후보곡 Feature -> Vector 변환
            Map<String, Double> recommendVectorMap = featureVectorizer.vectorizeSongMap(songFeatureDto);

            // 코사인 유사도 계산 (두 벡터가 이미 정규화되었으므로 단순 내적만 수행)
            double score = CosineSimilarity.compute(userVector, recommendVectorMap);

            // 점수가 0 초과인 후보곡만 저장
            if (score > 0) {
                scoredSongList.add(new SongRecommendationScoreDto(songFeatureDto, score));
            }
        }
        return scoredSongList;
    }


    /**
     * 추천 상위 50곡 추출 (PriorityQueue 적용)
     */
    private List<Long> getTopSongIdList(List<SongRecommendationScoreDto> scoredSongList) {

        final int LIMIT = 50;

        // score 기준 오름차순 Min-Heap
        PriorityQueue<SongRecommendationScoreDto> heap = new PriorityQueue<>(Comparator.comparingDouble(SongRecommendationScoreDto::getScore));

        // 모든 후보 순회
        for (SongRecommendationScoreDto dto : scoredSongList) {
            // 일단 추가
            heap.offer(dto);

            // 50개 초과하면 가장 작은 점수 제거
            if (heap.size() > LIMIT) {
                heap.poll();
            }
        }

        // heap → 정렬 → songId 추출
        return heap.stream()
                .sorted(Comparator.comparingDouble(SongRecommendationScoreDto::getScore).reversed())
                .map(dto -> dto.getSongFeatureDto().getSongId())
                .toList();
    }
}