package com.example.peachmusic.domain.album.repository;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CursorParam;
import com.example.peachmusic.common.query.SearchWordCondition;
import com.example.peachmusic.common.repository.KeysetPolicy;
import com.example.peachmusic.domain.album.dto.response.AlbumArtistDetailResponseDto;
import com.example.peachmusic.domain.album.dto.response.AlbumSearchResponseDto;
import com.example.peachmusic.domain.albumlike.entity.QAlbumLike;
import com.example.peachmusic.domain.artist.entity.QArtist;
import com.example.peachmusic.domain.artistalbum.entity.QArtistAlbum;
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
import static com.example.peachmusic.domain.artistalbum.entity.QArtistAlbum.artistAlbum;
import static com.example.peachmusic.domain.song.entity.QSong.song;

public class AlbumCustomRepositoryImpl implements AlbumCustomRepository {

    private final JPAQueryFactory queryFactory;
    private final KeysetPolicy keysetPolicy;

    public AlbumCustomRepositoryImpl(EntityManager em, KeysetPolicy keysetPolicy) {
        queryFactory = new JPAQueryFactory(em);
        this.keysetPolicy = keysetPolicy;
    }

    /**
     * 검색 - 자세히 보기
     */
    @Override
    public List<AlbumSearchResponseDto> findAlbumKeysetPageByWord(AuthUser authUser, String word, int size, boolean isAdmin, SortType sortType, SortDirection direction, CursorParam cursor) {
        return baseQuery(authUser, word, isAdmin, sortType, direction, cursor).limit(size+1).fetch(); // 요청한 사이즈보다 하나 더 많은 데이터를 조회
    }

    /**
     * 검색 - 미리보기
     */
    @Override
    public List<AlbumSearchResponseDto> findAlbumListByWord(AuthUser authUser, String word, int size, boolean isAdmin, SortType sortType, SortDirection direction) {
        return baseQuery(authUser, word, isAdmin, sortType, direction, null).limit(size).fetch();
    }

    /**
     * 특정 아티스트의 앨범 - 미리보기
     */
    @Override
    public List<AlbumArtistDetailResponseDto> findAlbumList(AuthUser authUser, Long artistId, int size) {
        return baseQueryByArtist(authUser, artistId, SortType.RELEASE_DATE, SortDirection.DESC, null).limit(size).fetch();
    }

    /**
     * 특정 아티스트의 앨범 - 자세히 보기
     */
    @Override
    public List<AlbumArtistDetailResponseDto> findAlbumByArtistKeyset(AuthUser authUser, Long artistId, SortType sortType, SortDirection sortDirection, CursorParam cursor, int size) {
        return baseQueryByArtist(authUser, artistId, sortType, sortDirection, cursor).limit(size + 1).fetch();
    }

    /**
     * 기본 쿼리
     */
    private JPAQuery<AlbumSearchResponseDto> baseQuery(AuthUser authUser, String word, boolean isAdmin, SortType sortType, SortDirection direction, CursorParam cursor) {

        keysetPolicy.validateCursor(sortType, cursor); // 커서 검증
        boolean isAsc = keysetPolicy.isAscending(sortType, direction);

        // 아티스트 이름을 문자열로 합치기
        StringTemplate artistNames = Expressions.stringTemplate("GROUP_CONCAT({0})", artist.artistName);

        return baseFrom()
                .select(Projections.constructor(AlbumSearchResponseDto.class, album.albumId, album.albumName, artistNames, album.albumReleaseDate, album.albumImage, album.likeCount, isAlbumLiked(authUser), album.isDeleted))
                .where(searchCondition(word), isActive(isAdmin), keysetCondition(sortType, isAsc, cursor)) // 검색어 조건, Keyset 조건
                .groupBy(album.albumId) // 아티스트 이름을 문자열로 합치는데 앨범 id를 기준으로 함
                .orderBy(keysetOrder(sortType, isAsc)); // Keyset 조건에 사용되는 커서 순서대로 정렬
    }

    /**
     * 아티스트 상세 전용 공통 쿼리
     */
    private JPAQuery<AlbumArtistDetailResponseDto> baseQueryByArtist(AuthUser authUser, Long artistId, SortType sortType, SortDirection direction, CursorParam cursor) {

        keysetPolicy.validateCursor(sortType, cursor);
        boolean isAsc = direction == SortDirection.ASC;

        StringTemplate artistNames = Expressions.stringTemplate("GROUP_CONCAT({0})", artist.artistName);

        return baseFrom()
                .select(Projections.constructor(AlbumArtistDetailResponseDto.class, album.albumId, album.albumName, artistNames, album.albumReleaseDate, album.albumImage, album.likeCount, album.isDeleted, isAlbumLiked(authUser)))
                .where(artist.artistId.eq(artistId), isActive(false), keysetCondition(sortType, isAsc, cursor))
                .groupBy(album.albumId)
                .orderBy(keysetOrder(sortType, isAsc));
    }

    private JPAQuery<?> baseFrom() {
        return queryFactory
                .from(album)
                .join(artistAlbum).on(artistAlbum.album.eq(album))
                .join(artist).on(artistAlbum.artist.eq(artist));
    }

    private Expression<Boolean> isAlbumLiked(AuthUser authUser) {

        if (authUser == null) {
            return Expressions.constant(false);
        }

        QAlbumLike sub = new QAlbumLike("subAlbumLike");

        return JPAExpressions
                .selectOne()
                .from(sub)
                .where(
                        sub.album.eq(album),
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
            condition = addCondition(condition, SearchWordCondition.wordMatch(album.albumName, w).or(artistNameExists(w)));
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
        QArtistAlbum subArtistAlbum = new QArtistAlbum("subArtistAlbum");

        return JPAExpressions // EXISTS 상관 서브쿼리: 존재 여부만 중요함
                .selectOne()
                .from(subArtistAlbum)
                .join(subArtistAlbum.artist, subArtist)
                .where(
                        subArtistAlbum.album.eq(album), // 메인 쿼리의 album과 연결
                        SearchWordCondition.wordMatch(subArtist.artistName, word) // 검색어가 아티스트 이름에 포함된 경우
                )
                .exists();
    }

    /**
     * 검색 조건
     * - 앨범이 삭제된 상태가 아닌 경우
     * - 음원이 하나라도 활성화 상태인 경우
     */
    private BooleanExpression isActive(boolean isAdmin) {
        if (isAdmin) {
            return null;
        }
        return album.isDeleted.isFalse().and(
                JPAExpressions
                        .selectOne()
                        .from(song)
                        .where(
                                song.album.eq(album),
                                song.isDeleted.isFalse(),
                                song.streamingStatus.isTrue()
                        )
                        .exists()
        );
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
            default -> throw new CustomException(ErrorCode.UNSUPPORTED_SORT_TYPE);
        };
    }

    /**
     * id가 Keyset이 되는 경우
     */
    private BooleanExpression idKeyset(boolean asc, Long lastId) {
        return asc ? album.albumId.gt(lastId) : album.albumId.lt(lastId);
    }

    /**
     * 좋아요 수가 Keyset이 되는 경우
     */
    private BooleanExpression likeCountKeyset(boolean asc, CursorParam cursor) {
        BooleanExpression likeCondition = asc ? album.likeCount.gt(cursor.getLastLike()) : album.likeCount.lt(cursor.getLastLike());
        return likeCondition.or(album.likeCount.eq(cursor.getLastLike()).and(idKeyset(asc, cursor.getLastId())));
    }

    /**
     * 이름이 Keyset이 되는 경우
     */
    private BooleanExpression nameKeyset(boolean asc, CursorParam cursor) {
        BooleanExpression nameCondition = asc ? album.albumName.gt(cursor.getLastName()) : album.albumName.lt(cursor.getLastName());
        return nameCondition.or(album.albumName.eq(cursor.getLastName()).and(idKeyset(asc, cursor.getLastId())));
    }

    /**
     * 날짜가 Keyset이 되는 경우
     */
    private BooleanExpression dateKeyset(boolean asc, CursorParam cursor) {
        BooleanExpression dateCondition = asc ? album.albumReleaseDate.gt(cursor.getLastDate()) : album.albumReleaseDate.lt(cursor.getLastDate());
        return dateCondition.or(album.albumReleaseDate.eq(cursor.getLastDate()).and(idKeyset(asc, cursor.getLastId())));
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
            case LIKE -> asc ? album.likeCount.asc() : album.likeCount.desc();
            case NAME -> asc ? album.albumName.asc() : album.albumName.desc();
            case RELEASE_DATE -> asc ? album.albumReleaseDate.asc() : album.albumReleaseDate.desc();
            default -> throw new CustomException(ErrorCode.UNSUPPORTED_SORT_TYPE);
        };
    }

    /**
     * id 정렬
     */
    private OrderSpecifier<Long> idOrder(boolean asc) {
        return asc ? album.albumId.asc(): album.albumId.desc();
    }
}
