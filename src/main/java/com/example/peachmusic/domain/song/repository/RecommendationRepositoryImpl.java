package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.domain.song.dto.SongFeatureDto;
import com.example.peachmusic.domain.song.dto.response.SongRecommendationResponseDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
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
    public Map<Long, SongFeatureDto> findFeatureBySongIdList(List<Long> songIdList) {
        // Seed 데이터가 없으면 종료
        if(songIdList == null || songIdList.isEmpty()) {
            return Collections.emptyMap();
        }

        // feature 조회
        List<Tuple> results = queryFactory
                .select(song.songId, genre.genreName, song.speed, song.vartags, song.instruments)
                .from(song)
                .leftJoin(songGenre).on(song.songId.eq(songGenre.song.songId))
                .leftJoin(genre).on(genre.genreId.eq(songGenre.genre.genreId))
                .where(inSeed(songIdList), hasAllFeature())
                .fetch();

        return convertMap(results);
    }

    // 추천 후보군 500개
    @Override
    public Map<Long, SongFeatureDto> findRecommendFeatureList(List<Long> songIdList) {
        // Seed 데이터가 없으면 종료
        if(songIdList == null || songIdList.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Tuple> results = queryFactory
                .select(song.songId, genre.genreName, song.speed, song.vartags, song.instruments)
                .from(song)
                .leftJoin(songGenre).on(song.songId.eq(songGenre.song.songId))
                .leftJoin(genre).on(genre.genreId.eq(songGenre.genre.genreId))
                .where(notInSeed(songIdList), hasAllFeature())
                .orderBy(song.likeCount.desc())
                .limit(500)
                .fetch();

        return convertMap(results);
    }

    /**
     * 최종 추천 결과 조회
     */
    @Override
    public Slice<SongRecommendationResponseDto> findRecommendedSongList(List<Long> orderBySongIdList, Pageable pageable) {
        // 추천 대상 음원 상세 조회
        List<SongRecommendationResponseDto> result = queryFactory
                .select(Projections.constructor(
                        SongRecommendationResponseDto.class,
                        song.songId,
                        song.name,
                        artist.artistId,
                        artist.artistName,
                        album.albumId,
                        album.albumName,
                        album.albumImage,
                        song.likeCount))
                .from(song)
                .leftJoin(song.album, album)
                .leftJoin(artistSong).on(artistSong.song.eq(song))
                .leftJoin(artistSong.artist, artist)
                .where(inOrder(orderBySongIdList))
                .fetch();

        // songId기준 Map변환
        Map<Long, SongRecommendationResponseDto> songMap = result.stream()
                .collect(Collectors.toMap(SongRecommendationResponseDto::getSongId, s -> s));

        // 추천 순서 유지(높은 순위부터 반환) 및 페이징 처리
        List<SongRecommendationResponseDto> sortedResult = orderBySongIdList.stream()
                .map(songMap::get)
                .filter(Objects::nonNull)
                .skip(pageable.getOffset())             // 시작 지점 무시 (offset)
                .limit(pageable.getPageSize() + 1)      // 필요한 개수만큼만 (pageSize + 1)
                .collect(Collectors.toCollection(ArrayList::new));

        return checkEndPage(sortedResult, pageable);
    }

    /**
     * cold-start일 경우 likeCount기준으로 추천 반환
     */
    @Override
    public Slice<SongRecommendationResponseDto> findRecommendedSongsForColdStart(Pageable pageable) {
        Pageable fixePageable = PageRequest.of(0, 50);

        List<SongRecommendationResponseDto> result = queryFactory
                .select(Projections.constructor(
                        SongRecommendationResponseDto.class,
                        song.songId,
                        song.name,
                        artist.artistId,
                        artist.artistName,
                        album.albumId,
                        album.albumName,
                        album.albumImage,
                        song.likeCount))
                .from(song)
                .leftJoin(song.album, album)
                .leftJoin(artistSong).on(artistSong.song.eq(song))
                .leftJoin(artistSong.artist, artist)
                .orderBy(song.likeCount.desc())
                .limit(50)
                .fetch();

        return new SliceImpl<>(result, fixePageable, false);
    }

    // 타음페이지 존재 여부 계산
    private <T> Slice<T> checkEndPage(List<T> commentList, Pageable pageable) {
        boolean hasNext = false;
        if (commentList.size() > pageable.getPageSize()) {
            hasNext = true;
            commentList.remove(pageable.getPageSize());
        }
        return new SliceImpl<>(commentList, pageable, hasNext);
    }

    // Tuple -> SongFeatureDto Map으로 변환(.transfrom 사용시 오류 발생)
    private Map<Long, SongFeatureDto> convertMap(List<Tuple> results) {
        Map<Long, SongFeatureDto> resultMap = new LinkedHashMap<>();

        for(Tuple row : results) {
            Long songId = row.get(song.songId);
            String genreName = row.get(genre.genreName);

            // 이미 존재하는 dto 조회
            SongFeatureDto songInResultMap = resultMap.get(songId);

            // 최초 생성 시
            if(songInResultMap == null) {

                List<String> genreList = new ArrayList<>();

                SongFeatureDto newSong = new SongFeatureDto(
                        songId,
                        genreList,
                        row.get(song.speed),
                        row.get(song.vartags),
                        row.get(song.instruments)
                );

                resultMap.put(songId, newSong);

                songInResultMap = newSong;
            }

            // 장르 누적
            if(genreName != null) {
                songInResultMap.getGenreNameList().add(genreName);
            }
        }
        return resultMap;
    }

    private BooleanExpression inOrder(List<Long> songIdList) {
        return (songIdList == null || songIdList.isEmpty()) ? null : song.songId.in(songIdList);
    }

    private BooleanExpression inSeed(List<Long> songIdList) {
        return (songIdList == null || songIdList.isEmpty()) ? null : song.songId.in(songIdList);
    }

    private BooleanExpression notInSeed(List<Long> songIdList) {
        return (songIdList == null || songIdList.isEmpty()) ? null : song.songId.notIn(songIdList);
    }

    private BooleanExpression hasAllFeature() {
        return song.speed.isNotNull()
                .and(song.vartags.isNotNull())
                .and(song.instruments.isNotNull());
    }
}