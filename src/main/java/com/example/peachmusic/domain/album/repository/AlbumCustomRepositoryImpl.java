package com.example.peachmusic.domain.album.repository;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.domain.album.dto.response.AlbumSearchResponseDto;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.util.StringUtils;
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
    public List<AlbumSearchResponseDto> findAlbumKeysetPageByWord(String word, int size, boolean isAdmin, SortType sortType, SortDirection direction, Long lastId, Long lastLike, String lastName) {
        return baseQuery(word, isAdmin, sortType, direction, lastId, lastLike, lastName)
                .limit(size+1).fetch(); // 요청한 사이즈보다 하나 더 많은 데이터를 조회
    }

    /**
     * 검색 - 미리보기
     */
    @Override
    public List<AlbumSearchResponseDto> findAlbumListByWord(String word, int size, boolean isAdmin, SortType sortType, SortDirection direction) {
        return baseQuery(word, isAdmin, sortType, direction, null, null, null).limit(size).fetch();
    }

    /**
     * 기본 쿼리
     */
    private JPAQuery<AlbumSearchResponseDto> baseQuery(String word, boolean isAdmin, SortType sortType, SortDirection direction, Long lastId, Long lastLike, String lastName) {

        boolean isAsc = direction == SortDirection.ASC;

        List<OrderSpecifier<?>> orders = new ArrayList<>();
        OrderSpecifier<?> main = mainOrder(sortType, isAsc);
        if (main != null) {
            orders.add(main);
        }
        orders.add(idOrder(isAsc)); // id 정렬은 항상 함

        return queryFactory // todo - artist.artistName List를 String으로 가공하기
                .selectDistinct(Projections.constructor(AlbumSearchResponseDto.class, album.albumId, album.albumName, artist.artistName, album.albumReleaseDate, album.albumImage, album.likeCount, album.isDeleted))
                .from(album)
                .join(artistAlbum).on(artistAlbum.album.eq(album))
                .join(artist).on(artistAlbum.artist.eq(artist))
                .where(searchCondition(word, isAdmin), keysetCondition(sortType, isAsc, lastId, lastLike, lastName)) // 검색어 포함 조건
                .orderBy(orders.toArray(OrderSpecifier[]::new));
    }

    /**
     * 검색 조건
     */
    private BooleanExpression searchCondition(String word, boolean isAdmin) {

        if (isAdmin){ // 관리자용은 삭제된 앨범도 조회되어야 함
            return albumNameContains(word);
        }

        BooleanExpression keywordCondition = albumNameContains(word).or(artistNameContains(word));
        return keywordCondition.and(isActive());
    }

    /**
     * 검색 조건 1
     * - 검색어가 앨범 이름에 포함된 경우
     */
    private BooleanExpression albumNameContains(String word) {
        return StringUtils.hasText(word) ? album.albumName.contains(word) : null;
    }

    /**
     * 검색 조건 2
     * - 검색어가 아티스트 이름에 포함된 경우
     */
    private BooleanExpression artistNameContains(String word) {
        return StringUtils.hasText(word) ? artist.artistName.startsWith(word) : null;
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
     * 좋아요 수가 Keyset이 되는 경우
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
