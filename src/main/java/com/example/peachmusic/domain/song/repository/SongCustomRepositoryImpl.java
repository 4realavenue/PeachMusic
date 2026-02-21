package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CursorParam;
import com.example.peachmusic.common.query.SearchWordCondition;
import com.example.peachmusic.common.repository.KeysetPolicy;
import com.example.peachmusic.domain.album.dto.response.SongSummaryDto;
import com.example.peachmusic.domain.song.dto.response.SongArtistDetailResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
import com.example.peachmusic.domain.songlike.entity.QSongLike;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import static com.example.peachmusic.domain.album.entity.QAlbum.album;
import static com.example.peachmusic.domain.artist.entity.QArtist.artist;
import static com.example.peachmusic.domain.artistsong.entity.QArtistSong.artistSong;
import static com.example.peachmusic.domain.song.entity.QSong.song;
import static com.example.peachmusic.domain.songprogressingstatus.entity.QSongProgressingStatus.songProgressingStatus;

public class SongCustomRepositoryImpl implements SongCustomRepository {

    private final JPAQueryFactory queryFactory;
    private final KeysetPolicy keysetPolicy;

    public SongCustomRepositoryImpl(EntityManager em, KeysetPolicy keysetPolicy) {
        queryFactory = new JPAQueryFactory(em);
        this.keysetPolicy = keysetPolicy;
    }
    /**
     * 검색 - 자세히 보기
     */
    @Override
    public List<SongSearchResponseDto> findSongKeysetPageByWord(AuthUser authUser, String word, int size, boolean isAdmin, SortType sortType, SortDirection direction, CursorParam cursor) {
        return baseQuery(authUser, word, isAdmin, sortType, direction, cursor)
                .limit(size+1).fetch(); // 요청한 사이즈보다 하나 더 많은 데이터를 조회
    }

    /**
     * 검색 - 미리보기
     */
    @Override
    public List<SongSearchResponseDto> findSongListByWord(AuthUser authUser, String word, int size, boolean isAdmin, SortType sortType, SortDirection direction) {
        return baseQuery(authUser, word, isAdmin, sortType, direction, null).limit(size).fetch();
    }

    /**
     * 음원 - 미리보기
     */
    @Override
    public List<SongArtistDetailResponseDto> findSongList(AuthUser authUser, Long artistId, int size) {
        return baseQueryByArtist(authUser, artistId, SortType.RELEASE_DATE, SortDirection.DESC, null).limit(size).fetch();
    }

    /**
     * 음원 - 자세히 보기
     */
    @Override
    public List<SongArtistDetailResponseDto> findSongByArtistKeyset(AuthUser authUser, Long artistId, SortType sortType, SortDirection sortDirection, CursorParam cursor, int size) {
        return baseQueryByArtist(authUser, artistId, sortType, sortDirection, cursor).limit(size + 1).fetch();
    }

    @Override
    public List<SongSummaryDto> findSongSummaryListByAlbumId(Long albumId) {
        return queryFactory
                .select(Projections.constructor(SongSummaryDto.class, song.position, song.songId, song.name, song.duration, song.likeCount))
                .from(song)
                .where(song.album.albumId.eq(albumId), song.isDeleted.isFalse(), song.streamingStatus.isTrue())
                .orderBy(song.position.asc(), song.songId.asc())
                .fetch();
    }

    /**
     * 기본 쿼리
     */
    private JPAQuery<SongSearchResponseDto> baseQuery(AuthUser authUser, String word, boolean isAdmin, SortType sortType, SortDirection direction, CursorParam cursor) {

        keysetPolicy.validateCursor(sortType, cursor); // 커서 검증
        boolean isAsc = keysetPolicy.isAscending(sortType, direction);

        // 아티스트 이름을 문자열로 합치기
        StringTemplate artistNames = Expressions.stringTemplate("GROUP_CONCAT({0})", artist.artistName);

        return baseFrom()
                .select(Projections.constructor(SongSearchResponseDto.class, song.songId, song.name, artistNames, song.releaseDate, album.albumImage, song.likeCount, isSongLiked(authUser), song.playCount, song.isDeleted, songProgressingStatus.progressingStatus))
                .where(searchCondition(word), isActive(isAdmin), keysetCondition(sortType, isAsc, cursor)) // 검색어 조건, Keyset 조건
                .groupBy(song.songId) // 아티스트 이름을 문자열로 합치는데 음원 id를 기준으로 함
                .orderBy(keysetOrder(sortType, isAsc)); // Keyset 조건에 사용되는 커서 순서대로 정렬
    }

    /**
     * 아티스트 상세 전용 공통 쿼리
     */
    private JPAQuery<SongArtistDetailResponseDto> baseQueryByArtist(AuthUser authUser, Long artistId, SortType sortType, SortDirection direction, CursorParam cursor) {

        keysetPolicy.validateCursor(sortType, cursor);
        boolean isAsc = direction == SortDirection.ASC;

        StringTemplate artistNames = Expressions.stringTemplate("GROUP_CONCAT({0})", artist.artistName);

        return baseFrom()
                .select(Projections.constructor(SongArtistDetailResponseDto.class, song.songId, song.name, artistNames, song.likeCount, album.albumImage, songProgressingStatus.progressingStatus, isSongLiked(authUser), album.albumId, song.releaseDate))
                .where(artist.artistId.eq(artistId), isActive(false), keysetCondition(sortType, isAsc, cursor))
                .groupBy(song.songId)
                .orderBy(keysetOrder(sortType, isAsc));
    }

    private JPAQuery<?> baseFrom() {
        return queryFactory
                .from(song)
                .join(artistSong).on(artistSong.song.eq(song))
                .join(artist).on(artistSong.artist.eq(artist))
                .join(song.album, album)
                .leftJoin(songProgressingStatus).on(songProgressingStatus.song.eq(song));
    }

    private Expression<Boolean> isSongLiked(AuthUser authUser) {

        if (authUser == null) {
            return Expressions.constant(false);
        }

        QSongLike sub = new QSongLike("subSongLike");

        return JPAExpressions
                .selectOne()
                .from(sub)
                .where(
                        sub.song.eq(song),
                        sub.user.userId.eq(authUser.getUserId())
                )
                .exists();
    }

    /**
     * 검색 조건
     */
    private BooleanExpression searchCondition(String word) {

        if (word == null) {
            return null;
        }

        BooleanExpression condition = null;

        for (String w : word.split("\\s+")) { // 검색 단어가 여러개인 경우 하나씩 조건에 넣어서 and로 묶음
            BooleanExpression albumMatch = SearchWordCondition.wordMatch(song.name, w);
            BooleanExpression artistMatch = SearchWordCondition.wordMatch(artist.artistName, w);
            condition = addCondition(condition, albumMatch.or(artistMatch));
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
     * - 음원이 삭제된 상태가 아닌 경우
     */
    private BooleanExpression isActive(boolean isAdmin) {
        if (isAdmin) {
            return null;
        }
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
        BooleanExpression dateCondition = asc ? song.releaseDate.gt(cursor.getLastDate()) : song.releaseDate.lt(cursor.getLastDate());
        return dateCondition.or(song.releaseDate.eq(cursor.getLastDate()).and(idKeyset(asc, cursor.getLastId())));
    }

    /**
     * 재생이 Keyset이 되는 경우
     */
    private BooleanExpression playKeyset(boolean asc, CursorParam cursor) {
        BooleanExpression dateCondition = asc ? song.playCount.gt(cursor.getLastPlay()) : song.playCount.lt(cursor.getLastPlay());
        return dateCondition.or(song.playCount.eq(cursor.getLastPlay()).and(idKeyset(asc, cursor.getLastId())));
    }
    /**
     * keyset 정렬
     */
    private OrderSpecifier<?>[] keysetOrder(SortType sortType, boolean isAsc) {

        OrderSpecifier<?> main = mainOrder(sortType, isAsc);

        if (main != null) {
            return new OrderSpecifier<?>[] {main, idOrder(isAsc)};
        }

        return new OrderSpecifier<?>[] {idOrder(isAsc)};
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
            case RELEASE_DATE -> asc ? song.releaseDate.asc() : song.releaseDate.desc();
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
