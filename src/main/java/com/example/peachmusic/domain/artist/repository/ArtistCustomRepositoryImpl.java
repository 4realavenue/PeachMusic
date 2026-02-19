package com.example.peachmusic.domain.artist.repository;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.CursorParam;
import com.example.peachmusic.common.query.SearchWordCondition;
import com.example.peachmusic.common.repository.KeysetPolicy;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import static com.example.peachmusic.domain.artist.entity.QArtist.artist;

public class ArtistCustomRepositoryImpl implements ArtistCustomRepository {

    private final JPAQueryFactory queryFactory;
    private final KeysetPolicy keysetPolicy;

    public ArtistCustomRepositoryImpl(EntityManager em, KeysetPolicy keysetPolicy) {
        queryFactory = new JPAQueryFactory(em);
        this.keysetPolicy = keysetPolicy;
    }

    /**
     * 검색 - 자세히 보기
     */
    @Override
    public List<ArtistSearchResponseDto> findArtistKeysetPageByWord(String word, int size, boolean isAdmin, SortType sortType, SortDirection direction, CursorParam cursor) {
        return baseQuery(word, isAdmin, sortType, direction, cursor).limit(size+1).fetch(); // 요청한 사이즈보다 하나 더 많은 데이터를 조회
    }

    /**
     * 검색 - 미리보기
     */
    @Override
    public List<ArtistSearchResponseDto> findArtistListByWord(String word, int size, boolean isAdmin, SortType sortType, SortDirection direction) {
        return baseQuery(word, isAdmin, sortType, direction, null).limit(size).fetch();
    }

    /**
     * 기본 쿼리
     */
    private JPAQuery<ArtistSearchResponseDto> baseQuery(String word, boolean isAdmin, SortType sortType, SortDirection direction, CursorParam cursor) {

        keysetPolicy.validateCursor(sortType, cursor); // 커서 검증
        boolean isAsc = keysetPolicy.isAscending(sortType, direction);

        return queryFactory
                .select(Projections.constructor(ArtistSearchResponseDto.class, artist.artistId, artist.artistName, artist.likeCount, artist.isDeleted))
                .from(artist)
                .where(searchCondition(word), isActive(isAdmin), keysetCondition(sortType, isAsc, cursor)) // 검색어 조건, Keyset 조건
                .orderBy(keysetOrder(sortType, isAsc)); // Keyset 조건에 사용되는 커서 순서대로 정렬
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
            condition = addCondition(condition, SearchWordCondition.wordMatch(artist.artistName, w));
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
     * - 아티스트가 삭제된 상태가 아닌 경우
     */
    private BooleanExpression isActive(boolean isAdmin) {
        if (isAdmin) {
            return null;
        }
        return artist.isDeleted.isFalse();
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
            default -> throw new CustomException(ErrorCode.UNSUPPORTED_SORT_TYPE);
        };
    }

    /**
     * id가 Keyset이 되는 경우
     */
    private BooleanExpression idKeyset(boolean asc, Long lastId) {
        return asc ? artist.artistId.gt(lastId) : artist.artistId.lt(lastId);
    }

    /**
     * 좋아요 수가 Keyset이 되는 경우
     */
    private BooleanExpression likeCountKeyset(boolean asc, CursorParam cursor) {
        BooleanExpression likeCondition = asc ? artist.likeCount.gt(cursor.getLastLike()) : artist.likeCount.lt(cursor.getLastLike());
        return likeCondition.or(artist.likeCount.eq(cursor.getLastLike()).and(idKeyset(asc, cursor.getLastId())));
    }

    /**
     * 이름이 Keyset이 되는 경우
     */
    private BooleanExpression nameKeyset(boolean asc, CursorParam cursor) {
        BooleanExpression nameCondition = asc ? artist.artistName.gt(cursor.getLastName()) : artist.artistName.lt(cursor.getLastName());
        return nameCondition.or(artist.artistName.eq(cursor.getLastName()).and(idKeyset(asc, cursor.getLastId())));
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
            case LIKE -> asc ? artist.likeCount.asc() : artist.likeCount.desc();
            case NAME -> asc ? artist.artistName.asc() : artist.artistName.desc();
            default -> throw new CustomException(ErrorCode.UNSUPPORTED_SORT_TYPE);
        };
    }

    /**
     * id 정렬
     */
    private OrderSpecifier<Long> idOrder(boolean asc) {
        return asc ? artist.artistId.asc(): artist.artistId.desc();
    }
}
