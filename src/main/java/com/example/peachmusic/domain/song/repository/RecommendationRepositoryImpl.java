package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.domain.song.dto.SongFeatureDto;
import com.example.peachmusic.domain.song.dto.response.SongRecommendationResponseDto;
import com.example.peachmusic.domain.song.entity.Song;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
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
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.types.Projections.list;

public class RecommendationRepositoryImpl implements RecommendationRepository {

    private final JPAQueryFactory queryFactory;

    public RecommendationRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    // 사용자가 좋아요/플레이리스트 시드 곡 정보 조회(User Profile 생성을 위해서)
    @Override
    public Map<Long, SongFeatureDto> findFeatureBySongIdList(List<Long> songIdList) {
        if(songIdList == null || songIdList.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Tuple> results = queryFactory
                .select(song.songId, genre.genreName, song.speed, song.vartags, song.instruments)
                .from(song)
                .leftJoin(songGenre).on(song.songId.eq(songGenre.song.songId))
                .leftJoin(genre).on(genre.genreId.eq(songGenre.genre.genreId))
                .where(song.songId.in(songIdList), song.speed.isNotNull(), song.vartags.isNotNull(), song.instruments.isNotNull())
                .fetch();

        return convertMap(results);
    }

    // 추천 후보군 500개
    @Override
    public Map<Long, SongFeatureDto> findCandidateFeatureList(List<Long> songIdList) {
        if(songIdList == null || songIdList.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Tuple> results = queryFactory
                .select(song.songId, genre.genreName, song.speed, song.vartags, song.instruments)
                .from(song)
                .leftJoin(songGenre).on(song.songId.eq(songGenre.song.songId))
                .leftJoin(genre).on(genre.genreId.eq(songGenre.genre.genreId))
                .where(song.songId.notIn(songIdList), song.speed.isNotNull(), song.vartags.isNotNull(), song.instruments.isNotNull())
                .orderBy(song.likeCount.desc())
                .limit(500)
                .fetch();

        return convertMap(results);
    }

    @Override
    public Slice<SongRecommendationResponseDto> getRecommendationSong(List<Long> orderBySongIdList, Pageable pageable) {
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
                .where(song.songId.in(orderBySongIdList))
                .fetch();

        Map<Long, SongRecommendationResponseDto> songMap = result.stream()
                .collect(Collectors.toMap(SongRecommendationResponseDto::getSongId, s -> s));

        List<SongRecommendationResponseDto> sortedResult = orderBySongIdList.stream()
                .map(songMap::get)
                .filter(Objects::nonNull)
                .skip(pageable.getOffset())             // 시작 지점 무시 (offset)
                .limit(pageable.getPageSize() + 1)      // 필요한 개수만큼만 (pageSize + 1)
                .collect(Collectors.toCollection(ArrayList::new));

        return checkEndPage(sortedResult, pageable);
    }

    private <T> Slice<T> checkEndPage(List<T> commentList, Pageable pageable) {
        boolean hasNext = false;
        if (commentList.size() > pageable.getPageSize()) {
            hasNext = true;
            commentList.remove(pageable.getPageSize());
        }
        return new SliceImpl<>(commentList, pageable, hasNext);
    }

    private Map<Long, SongFeatureDto> convertMap(List<Tuple> results) {
        Map<Long, SongFeatureDto> resultMap = new LinkedHashMap<>();

        for(Tuple row : results) {
            Long songId = row.get(song.songId);
            String genreName = row.get(genre.genreName);

            SongFeatureDto songInResultMap = resultMap.get(songId);

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

            if(genreName != null) {
                songInResultMap.getGenreNameList().add(genreName);
            }
        }
        return resultMap;
    }
}