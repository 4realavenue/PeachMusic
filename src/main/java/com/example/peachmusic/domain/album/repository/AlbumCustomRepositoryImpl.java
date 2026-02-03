package com.example.peachmusic.domain.album.repository;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.query.SearchWordCondition;
import com.example.peachmusic.domain.album.dto.response.AlbumSearchResponseDto;
import com.example.peachmusic.domain.artist.entity.QArtist;
import com.example.peachmusic.domain.artistalbum.entity.QArtistAlbum;
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
import static com.example.peachmusic.domain.artistalbum.entity.QArtistAlbum.artistAlbum;

public class AlbumCustomRepositoryImpl implements AlbumCustomRepository {

    private final JPAQueryFactory queryFactory;

    public AlbumCustomRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 검색 - 자세히 보기
     */
    @Override
    public List<AlbumSearchResponseDto> findAlbumKeysetPageByWord(String[] words, int size, boolean isAdmin, SortType sortType, SortDirection direction, Long lastId, Long lastLike, String lastName) {
        return baseQuery(words, isAdmin, sortType, direction, lastId, lastLike, lastName)
                .limit(size+1).fetch(); // 요청한 사이즈보다 하나 더 많은 데이터를 조회
    }

    /**
     * 검색 - 미리보기
     */
    @Override
    public List<AlbumSearchResponseDto> findAlbumListByWord(String[] words, int size, boolean isAdmin, SortType sortType, SortDirection direction) {
        return baseQuery(words, isAdmin, sortType, direction, null, null, null).limit(size).fetch();
    }

    /**
     * 기본 쿼리
     */
    private JPAQuery<AlbumSearchResponseDto> baseQuery(String[] words, boolean isAdmin, SortType sortType, SortDirection direction, Long lastId, Long lastLike, String lastName) {

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
                .select(Projections.constructor(AlbumSearchResponseDto.class, album.albumId, album.albumName, artistNames, album.albumReleaseDate, album.albumImage, album.likeCount, album.isDeleted))
                .from(album)
                .join(artistAlbum).on(artistAlbum.album.eq(album))
                .join(artist).on(artistAlbum.artist.eq(artist))
                .where(searchCondition(words, isAdmin), keysetCondition(sortType, isAsc, lastId, lastLike, lastName)) // 검색어 조건, Keyset 조건
                .groupBy(album.albumId) // 아티스트 이름을 문자열로 합치는데 앨범 id를 기준으로 함
                .orderBy(orderList.toArray(OrderSpecifier[]::new)); // Keyset 조건에 사용되는 커서 순서대로 정렬
    }

    /**
     * 검색 조건
     */
    private BooleanExpression searchCondition(String[] words, boolean isAdmin) {

        BooleanExpression condition = null;

        for (String w : words) { // 검색 단어가 여러개인 경우 하나씩 조건에 넣어서 and로 묶음
            condition = addCondition(condition, SearchWordCondition.wordMatch(album.albumName, w).or(artistNameExists(w)));
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
        QArtistAlbum subArtistAlbum = new QArtistAlbum("subArtistAlbum");

        return JPAExpressions // EXISTS 상관 서브쿼리: 존재 여부만 중요함
                .selectOne()
                .from(subArtistAlbum)
                .join(subArtistAlbum.artist, subArtist)
                .where(
                        subArtistAlbum.album.eq(album), // 메인 쿼리의 album과 연결
                        // 검색어가 아티스트 이름에 포함된 경우
                        Expressions.stringTemplate("concat(' ', {0}, ' ')", subArtist.artistName)
                                .like(Expressions.stringTemplate("concat('% ', {0}, ' %')", word))
                )
                .exists();
    }

    /**
     * 검색 조건 3
     * - 앨범이 삭제된 상태가 아닌 경우
     */
    private BooleanExpression isActive() {
        return album.isDeleted.isFalse();
    }

    /**
     * Keyset 조건
     */
    private BooleanExpression keysetCondition(SortType sortType, boolean asc, Long lastId, Long lastLike, String lastName) {

        // lastId가 없으면 Keyset 조건 없음
        if (lastId == null) {
            return null;
        }

        // 메인 정렬이 없는 경우 → id만
        if (sortType == null) {
            return idKeyset(asc, lastId);
        }

        return switch (sortType) {
            case LIKE -> likeCountKeyset(asc, lastId, lastLike);
            case NAME -> nameKeyset(asc, lastId, lastName);
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
    private BooleanExpression likeCountKeyset(boolean asc, Long lastId, Long lastLike) {
        BooleanExpression likeCondition = asc ? album.likeCount.gt(lastLike) : album.likeCount.lt(lastLike);
        return likeCondition.or(album.likeCount.eq(lastLike).and(idKeyset(asc, lastId)));
    }

    /**
     * 이름이 Keyset이 되는 경우
     */
    private BooleanExpression nameKeyset(boolean asc, Long lastId, String lastName) {
        BooleanExpression nameCondition = asc ? album.albumName.gt(lastName) : album.albumName.lt(lastName);
        return nameCondition.or(album.albumName.eq(lastName).and(idKeyset(asc, lastId)));
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
        };
    }

    /**
     * id 정렬
     */
    private OrderSpecifier<Long> idOrder(boolean asc) {
        return asc ? album.albumId.asc(): album.albumId.desc();
    }
}
