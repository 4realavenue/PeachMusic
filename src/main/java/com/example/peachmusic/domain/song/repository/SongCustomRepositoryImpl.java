package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.model.CursorParam;
import com.example.peachmusic.common.query.SearchWordCondition;
import com.example.peachmusic.domain.artist.entity.QArtist;
import com.example.peachmusic.domain.artistsong.entity.QArtistSong;
import com.example.peachmusic.domain.song.dto.response.SongArtistDetailResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import static com.example.peachmusic.domain.album.entity.QAlbum.album;
import static com.example.peachmusic.domain.artist.entity.QArtist.artist;
import static com.example.peachmusic.domain.artistsong.entity.QArtistSong.artistSong;
import static com.example.peachmusic.domain.song.entity.QSong.song;
import static com.example.peachmusic.domain.songlike.entity.QSongLike.songLike;
import static com.example.peachmusic.domain.streamingjob.entity.QStreamingJob.streamingJob;

public class SongCustomRepositoryImpl implements SongCustomRepository {

    private final JPAQueryFactory queryFactory;

    public SongCustomRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 검색 - 자세히 보기
     */
    @Override
    public List<SongSearchResponseDto> findSongKeysetPageByWord(String[] words, int size, boolean isAdmin, SortType sortType, SortDirection direction, CursorParam cursor) {
        return baseQuery(words, isAdmin, sortType, direction, cursor)
                .limit(size+1).fetch(); // 요청한 사이즈보다 하나 더 많은 데이터를 조회
    }

    /**
     * 검색 - 미리보기
     */
    @Override
    public List<SongSearchResponseDto> findSongListByWord(String[] words, int size, boolean isAdmin, SortType sortType, SortDirection direction) {
        return baseQuery(words, isAdmin, sortType, direction, null).limit(size).fetch();
    }

    /**
     * 음원 - 미리보기
     */
    @Override
    public List<SongArtistDetailResponseDto> findSongList(Long userId, Long artistId, int size) {
        return baseQueryByArtist(userId, artistId, SortType.RELEASE_DATE, SortDirection.DESC, null).limit(size).fetch();
    }

    /**
     * 음원 - 자세히 보기
     */
    @Override
    public List<SongArtistDetailResponseDto> findSongByArtistKeyset(Long userId, Long artistId, SortType sortType, SortDirection sortDirection, CursorParam cursor, int size) {
        return baseQueryByArtist(userId, artistId, sortType, sortDirection, cursor).limit(size + 1).fetch();
    }

    /**
     * 기본 쿼리
     */
    private JPAQuery<SongSearchResponseDto> baseQuery(String[] words, boolean isAdmin, SortType sortType, SortDirection direction, CursorParam cursor) {

        boolean isAsc = direction == SortDirection.ASC;

        List<OrderSpecifier<?>> orderList = new ArrayList<>();
        OrderSpecifier<?> main = mainOrder(sortType, isAsc);
        if (main != null) {
            orderList.add(main);
        }
        orderList.add(idOrder(isAsc)); // id 정렬은 항상 함

        // 아티스트 이름을 문자열로 합치기
        StringTemplate artistNames = Expressions.stringTemplate("GROUP_CONCAT({0})", artist.artistName);

        return queryFactory
                .select(Projections.constructor(SongSearchResponseDto.class, song.songId, song.name, artistNames, album.albumReleaseDate, song.album.albumImage, song.likeCount, song.playCount, song.isDeleted, streamingJob.jobStatus))
                .from(song)
                .join(artistSong).on(artistSong.song.eq(song))
                .join(artist).on(artistSong.artist.eq(artist))
                .join(song.album, album)
                .join(streamingJob).on(streamingJob.song.eq(song))
                .where(searchCondition(words, isAdmin), keysetCondition(sortType, isAsc, cursor)) // 검색어 조건, Keyset 조건
                .groupBy(song.songId) // 아티스트 이름을 문자열로 합치는데 음원 id를 기준으로 함
                .orderBy(orderList.toArray(OrderSpecifier[]::new)); // Keyset 조건에 사용되는 커서 순서대로 정렬
    }

    /**
     * 아티스트 상세 전용 공통 쿼리
     */
    private JPAQuery<SongArtistDetailResponseDto> baseQueryByArtist(Long userId, Long artistId, SortType sortType, SortDirection direction, CursorParam cursor) {

        boolean isAsc = direction == SortDirection.ASC;

        List<OrderSpecifier<?>> orderList = new ArrayList<>();
        OrderSpecifier<?> main = mainOrder(sortType, isAsc);
        if (main != null) {
            orderList.add(main);
        }
        orderList.add(idOrder(isAsc)); // id 정렬은 항상 함

        StringTemplate artistNames = Expressions.stringTemplate("GROUP_CONCAT({0})", artist.artistName);

        return queryFactory
                .select(Projections.constructor(SongArtistDetailResponseDto.class, song.songId, song.name, artistNames, song.likeCount, song.album.albumImage, streamingJob.jobStatus, songLike.songLikeId.isNotNull(), album.albumId, album.albumReleaseDate))
                .from(song)
                .join(artistSong).on(artistSong.song.eq(song))
                .join(artist).on(artistSong.artist.eq(artist))
                .join(song.album, album)
                .join(streamingJob).on(streamingJob.song.eq(song))
                .leftJoin(songLike).on(songLike.song.eq(song).and(songLike.user.userId.eq(userId)))
                .where(artist.artistId.eq(artistId), song.isDeleted.isFalse(), song.streamingStatus.isTrue(), keysetCondition(sortType, isAsc, cursor))
                .groupBy(song.songId)
                .orderBy(orderList.toArray(OrderSpecifier[]::new));
    }

    /**
     * 검색 조건
     */
    private BooleanExpression searchCondition(String[] words, boolean isAdmin) {

        if (words == null) {
            return null;
        }

        BooleanExpression condition = null;

        for (String w : words) { // 검색 단어가 여러개인 경우 하나씩 조건에 넣어서 and로 묶음
            condition = addCondition(condition, SearchWordCondition.wordMatch(song.name, w).or(artistNameExists(w)));
        }

        if (!isAdmin) {
            condition = addCondition(condition, isActive());
        }

        return condition;
    }

    /**
     * 검색 조건 더하기
     */
    private BooleanExpression addCondition(BooleanExpression condition1, BooleanExpression condition2) {
        return condition1 == null ? condition2 : condition1.and(condition2);
    }

    /**
     * 검색 조건
     * - `검색어가 이름에 포함된 아티스트`가 한명이라도 존재하는 경우
     */
    private BooleanExpression artistNameExists(String word) {
        QArtist subArtist = new QArtist("subArtist");
        QArtistSong subArtistSong = new QArtistSong("subArtistSong");

        return JPAExpressions // EXISTS 상관 서브쿼리: 존재 여부만 중요함
                .selectOne()
                .from(subArtistSong)
                .join(subArtistSong.artist, subArtist)
                .where(
                        subArtistSong.song.eq(song), // 메인 쿼리의 song과 연결
                        // 검색어가 아티스트 이름에 포함된 경우
                        Expressions.stringTemplate("concat(' ', {0}, ' ')", subArtist.artistName)
                                .like(Expressions.stringTemplate("concat('% ', {0}, ' %')", word))
                )
                .exists();
    }

    /**
     * 검색 조건
     * - 음원이 삭제된 상태가 아닌 경우
     */
    private BooleanExpression isActive() {
        return song.isDeleted.isFalse().and(song.streamingStatus.isTrue());
    }

    /**
     * Keyset 조건
     */
    private BooleanExpression keysetCondition(SortType sortType, boolean asc, CursorParam cursor) {

        // lastId가 없으면 Keyset 조건 없음
        if (cursor == null || cursor.getLastId() == null) {
            return null;
        }

        // 메인 정렬이 없는 경우 → id만
        if (sortType == null) {
            return idKeyset(asc, cursor.getLastId());
        }

        return switch (sortType) {
            case LIKE -> likeCountKeyset(asc, cursor);
            case NAME -> nameKeyset(asc, cursor);
            case RELEASE_DATE -> dateKeyset(asc, cursor);
            case PLAY -> playKeyset(asc, cursor);
        };
    }

    /**
     * id가 Keyset이 되는 경우
     */
    private BooleanExpression idKeyset(boolean asc, Long lastId) {
        return asc ? song.songId.gt(lastId) : song.songId.lt(lastId);
    }

    /**
     * 좋아요 수가 Keyset이 되는 경우
     */
    private BooleanExpression likeCountKeyset(boolean asc, CursorParam cursor) {
        BooleanExpression likeCondition = asc ? song.likeCount.gt(cursor.getLastLike()) : song.likeCount.lt(cursor.getLastLike());
        return likeCondition.or(song.likeCount.eq(cursor.getLastLike()).and(idKeyset(asc, cursor.getLastId())));
    }

    /**
     * 이름이 Keyset이 되는 경우
     */
    private BooleanExpression nameKeyset(boolean asc, CursorParam cursor) {
        BooleanExpression nameCondition = asc ? song.name.gt(cursor.getLastName()) : song.name.lt(cursor.getLastName());
        return nameCondition.or(song.name.eq(cursor.getLastName()).and(idKeyset(asc, cursor.getLastId())));
    }

    /**
     * 날짜가 Keyset이 되는 경우
     */
    private BooleanExpression dateKeyset(boolean asc, CursorParam cursor) {
        BooleanExpression dateCondition = asc ? album.albumReleaseDate.gt(cursor.getLastDate()) : album.albumReleaseDate.lt(cursor.getLastDate());
        return dateCondition.or(album.albumReleaseDate.eq(cursor.getLastDate()).and(idKeyset(asc, cursor.getLastId())));
    }

    /**
     * 재생이 Keyset이 되는 경우
     */
    private BooleanExpression playKeyset(boolean asc, CursorParam cursor) {
        BooleanExpression dateCondition = asc ? song.playCount.gt(cursor.getLastPlay()) : song.playCount.lt(cursor.getLastPlay());
        return dateCondition.or(song.playCount.eq(cursor.getLastPlay()).and(idKeyset(asc, cursor.getLastId())));
    }

    /**
     * 메인 정렬
     */
    private OrderSpecifier<?> mainOrder(SortType sortType, boolean asc) {
        if (sortType == null) {
            return null;
        }

        return switch (sortType) {
            case LIKE -> asc ? song.likeCount.asc() : song.likeCount.desc();
            case NAME -> asc ? song.name.asc() : song.name.desc();
            case RELEASE_DATE -> asc ? album.albumReleaseDate.asc() : album.albumReleaseDate.desc();
            case PLAY -> asc ? song.playCount.asc() : song.playCount.desc();
        };
    }

    /**
     * id 정렬
     */
    private OrderSpecifier<Long> idOrder(boolean asc) {
        return asc ? song.songId.asc(): song.songId.desc();
    }
}
