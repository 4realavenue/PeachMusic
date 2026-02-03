package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.domain.song.dto.SongFeatureDto;
import com.example.peachmusic.domain.song.dto.response.SongRecommendationResponseDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import java.util.*;
import java.util.stream.Collectors;
import static com.example.peachmusic.domain.album.entity.QAlbum.album;
import static com.example.peachmusic.domain.artist.entity.QArtist.artist;
import static com.example.peachmusic.domain.artistsong.entity.QArtistSong.artistSong;
import static com.example.peachmusic.domain.genre.entity.QGenre.genre;
import static com.example.peachmusic.domain.song.entity.QSong.song;
import static com.example.peachmusic.domain.songgenre.entity.QSongGenre.songGenre;

public class RecommendationRepositoryImpl implements RecommendationRepository {

    private final JPAQueryFactory queryFactory;

    public RecommendationRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    // 사용자가 좋아요/플레이리스트 시드 곡 정보 조회(User Profile 생성을 위해서)
    @Override
    public Map<Long, SongFeatureDto> findFeatureBySongIdMap(List<Long> songIdList) {
        // Seed 데이터가 없으면 종료
        if(songIdList == null || songIdList.isEmpty()) {
            return Collections.emptyMap();
        }

        // feature 조회
        List<Tuple> result = queryFactory
                .select(song.songId, genre.genreName, song.speed, song.vartags, song.instruments)
                .from(song)
                .leftJoin(songGenre).on(songGenre.song.eq(song))
                .leftJoin(genre).on(songGenre.genre.eq(genre))
                .where(song.songId.in(songIdList), hasAllFeature(), isStreamingSuccessStatus())
                .fetch();

        return convertMap(result);
    }

    // 추천 후보군 500개
    @Override
    public Map<Long, SongFeatureDto> findRecommendFeatureMap(List<Long> songIdList, List<Long> genreId) {
        // Seed 데이터가 없으면 종료
        if(songIdList == null || songIdList.isEmpty() || genreId == null || genreId.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Tuple> result = queryFactory
                .select(song.songId, genre.genreName, song.speed, song.vartags, song.instruments)
                .from(song)
                .leftJoin(songGenre).on(songGenre.song.eq(song))
                .leftJoin(genre).on(songGenre.genre.eq(genre))
                .where(song.songId.notIn(songIdList), hasAllFeature(), genre.genreId.in(genreId), isStreamingSuccessStatus())
                .orderBy(song.likeCount.desc())
                .limit(500)
                .fetch();

        return convertMap(result);
    }

    /**
     * 최종 추천 결과 조회
     */
    @Override
    public Slice<SongRecommendationResponseDto> findRecommendedSongSlice(List<Long> orderBySongIdList, Pageable pageable) {
        // 추천 대상 음원 상세 조회
        List<SongRecommendationResponseDto> result = queryFactory
                .select(Projections.constructor(SongRecommendationResponseDto.class, song.songId, song.name, artist.artistId, artist.artistName, album.albumId, album.albumName, album.albumImage, song.likeCount))
                .from(song)
                .leftJoin(song.album, album)
                .leftJoin(artistSong).on(artistSong.song.eq(song))
                .leftJoin(artistSong.artist, artist)
                .where(inOrder(orderBySongIdList), isStreamingSuccessStatus())
                .fetch();

        // songId기준 Map 변환
        Map<Long, SongRecommendationResponseDto> songMap = result.stream()
                .collect(Collectors.toMap(SongRecommendationResponseDto::getSongId, s -> s));

        // 추천 순서 유지(높은 순위부터 반환) 및 페이징 처리
        List<SongRecommendationResponseDto> sortedResult = orderBySongIdList.stream() // 추천 순서 기준
                .map(songMap::get) // ID -> DTO 변환
                .filter(Objects::nonNull) // 없는 데이터 필터링
                .skip(pageable.getOffset()) // offset만큼 건너뛴
                .limit(pageable.getPageSize() + 1) // size + 1
                .collect(Collectors.toCollection(ArrayList::new));

        // Slice 생성
        return checkEndPage(sortedResult, pageable);
    }

    /**
     * cold-start일 경우 likeCount기준으로 추천 반환
     */
    @Override
    public Slice<SongRecommendationResponseDto> findRecommendedSongSliceForColdStart(Pageable pageable) {

        // 인기순 음원 50건 조회
        List<SongRecommendationResponseDto> result = queryFactory
                .select(Projections.constructor(SongRecommendationResponseDto.class, song.songId, song.name, artist.artistId, artist.artistName, album.albumId, album.albumName, album.albumImage, song.likeCount))
                .from(song)
                .leftJoin(song.album, album)
                .leftJoin(artistSong).on(artistSong.song.eq(song))
                .leftJoin(artistSong.artist, artist)
                .where(isStreamingSuccessStatus())
                .orderBy(song.likeCount.desc())
                .limit(50)
                .fetch();

        // 페이징 처리
        List<SongRecommendationResponseDto> pageResult = result.stream()
                .skip(pageable.getOffset()) // 앞에서 offset만큼 스킵
                .limit(pageable.getPageSize() + 1) // size + 1개 가져오기(다음 페이지 확인용)
                .collect(Collectors.toCollection(ArrayList::new)); // list로 변환

        // Slice 생성
        return checkEndPage(pageResult, pageable);
    }

    @Override
    public List<Long> findSeedGenreList(List<Long> mergedSongIdList) {
        // Seed 데이터가 없으면 종료
        if(mergedSongIdList == null || mergedSongIdList.isEmpty()) {
            return Collections.emptyList();
        }

        return queryFactory
                .select(genre.genreId).distinct()
                .from(song)
                .join(songGenre).on(songGenre.song.eq(song))
                .join(genre).on(songGenre.genre.eq(genre))
                .where(song.songId.in(mergedSongIdList), isStreamingSuccessStatus())
                .fetch();
    }

    // 다음페이지 존재 여부 계산
    private <T> Slice<T> checkEndPage(List<T> songList, Pageable pageable) {
        boolean hasNext = false;
        // size 보다 하나 더 있으면 다음 페이지 존재
        if (songList.size() > pageable.getPageSize()) {
            hasNext = true;
            songList.remove(pageable.getPageSize()); // 마지막 제거
        }
        return new SliceImpl<>(songList, pageable, hasNext);
    }

    // Tuple -> SongFeatureDto Map으로 변환(.transfrom 사용시 오류 발생)
    private Map<Long, SongFeatureDto> convertMap(List<Tuple> result) {
        // 결과를 저장할 Map -> Key : songId, Value : 해당 곡의 FeatureDto
        Map<Long, SongFeatureDto> resultMap = new LinkedHashMap<>();

        // Db에서 조회한 모든 열을 하나씩 반복
        for(Tuple row : result) {
            // 현재 row의 곡 id
            Long songId = row.get(song.songId);
            // 현재 row의 장르 이름(Join 결과)
            String genreName = row.get(genre.genreName);

            // 이미 해당 곡의 DTO가 생성되어 있는지 확인
            SongFeatureDto songInResultMap = resultMap.get(songId);

            // 최초 생성 시
            if(songInResultMap == null) {
                // 장르 누적용 리스트(여러 장르가 한곡에 존재)
                List<String> genreList = new ArrayList<>();
                // SongFeatureDto 생성
                SongFeatureDto newSong = new SongFeatureDto(songId, genreList, row.get(song.speed), row.get(song.vartags), row.get(song.instruments));
                // Map에 저장
                resultMap.put(songId, newSong);
                // 이후 누적 처리를 위해서 참조로 연결
                songInResultMap = newSong;
            }

            // 장르 정보가 있으면 기존 Dto에 누적
            if(genreName != null) {
                songInResultMap.getGenreNameList().add(genreName);
            }
        }
        // songId 기준으로 묶인 FeatureMap 반환
        return resultMap;
    }

    private BooleanExpression inOrder(List<Long> songIdList) {
        return (songIdList == null || songIdList.isEmpty()) ? null : song.songId.in(songIdList);
    }

    private BooleanExpression hasAllFeature() {
        return song.speed.isNotNull()
                .and(song.vartags.isNotNull())
                .and(song.instruments.isNotNull());
    }

    private BooleanExpression isStreamingSuccessStatus() {
        return song.streamingStatus.isFalse();
    }
}