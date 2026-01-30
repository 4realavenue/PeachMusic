package com.example.peachmusic.domain.artist.repository;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Objects;
import static com.example.peachmusic.common.enums.UserRole.USER;
import static com.example.peachmusic.domain.artist.entity.QArtist.artist;

public class ArtistCustomRepositoryImpl implements ArtistCustomRepository {

    private final JPAQueryFactory queryFactory;

    public ArtistCustomRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 검색 - 자세히 보기
     */
    @Override
    public List<ArtistSearchResponseDto> findArtistKeysetPageByWord(String word, UserRole role, int size, SortType sortType, SortDirection direction, Long lastId, Long lastLike, String lastName) {
        return baseQuery(word, role, sortType, direction, lastId, lastLike, lastName)
                .limit(size+1).fetch(); // 요청한 사이즈보다 하나 더 많은 데이터를 조회
    }

    /**
     * 검색 - 미리보기
     */
    @Override
    public List<ArtistSearchResponseDto> findArtistListByWord(String word, UserRole role, int size, SortType sortType, SortDirection direction) {
        return baseQuery(word, role, sortType, direction, null, null, null).limit(size).fetch();
    }

    /**
     * 기본 쿼리
     */
    private JPAQuery<ArtistSearchResponseDto> baseQuery(String word, UserRole role, SortType sortType, SortDirection direction, Long lastId, Long lastLike, String lastName) {

        // 정렬 기본 -> 좋아요 내림차순
        sortType = sortType == null ? SortType.LIKE : sortType;
        boolean isAsc = direction == SortDirection.ASC;

        OrderSpecifier<?> main = mainOrder(sortType, isAsc);

        return queryFactory
                .select(Projections.constructor(ArtistSearchResponseDto.class, artist.artistId, artist.artistName, artist.likeCount, artist.isDeleted))
                .from(artist)
                .where(searchCondition(word, role), keysetCondition(sortType, isAsc, lastId, lastLike, lastName))
                .orderBy(main, idOrder(isAsc));
    }

    /**
     * 검색 조건
     */
    private BooleanExpression searchCondition(String word, UserRole role) {
        if (role == USER) {
            return Objects.requireNonNull(artistNameEquals(word)).and(isActive());
        }
        return artistNameEquals(word);
    }

    /**
     * 검색 조건 1
     * - 검색어가 아티스트 이름에 포함하는 경우
     */
    private BooleanExpression artistNameEquals(String word) {
        return StringUtils.hasText(word) ? artist.artistName.startsWith(word) : null;
    }

    /**
     * 검색 조건 2
     * - 아티스트가 삭제된 상태가 아닌 경우
     */
    private BooleanExpression isActive() {
        return artist.isDeleted.isFalse();
    }

    /**
     * Keyset 조건
     */
    private BooleanExpression keysetCondition(SortType sortType, boolean asc, Long lastId, Long lastLike, String lastName) {

        if (lastId == null) {
            return null;
        }

        if (sortType == SortType.LIKE && lastLike != null) {
            return likeCountKeyset(asc, lastId, lastLike);
        } else if (sortType == SortType.NAME && lastName != null) {
            return nameKeyset(asc, lastId, lastName);
        }
        return null;
    }

    /**
     * 좋아요 수가 Keyset이 되는 경우
     */
    private BooleanExpression likeCountKeyset(boolean asc, Long lastId, Long lastLike) {
        BooleanExpression likeCondition = asc ? artist.likeCount.gt(lastLike) : artist.likeCount.lt(lastLike);
        BooleanExpression idCondition = asc ? artist.artistId.gt(lastId) : artist.artistId.lt(lastId);
        return likeCondition.or(artist.likeCount.eq(lastLike).and(idCondition));
    }

    /**
     * 이름이 Keyset이 되는 경우
     */
    private BooleanExpression nameKeyset(boolean asc, Long lastId, String lastName) {
        BooleanExpression nameCondition = asc ? artist.artistName.gt(lastName) : artist.artistName.lt(lastName);
        BooleanExpression idCondition = asc ? artist.artistId.gt(lastId) : artist.artistId.lt(lastId);
        return nameCondition.or(artist.artistName.eq(lastName).and(idCondition));
    }

    /**
     * 메인 정렬
     */
    private OrderSpecifier<?> mainOrder(SortType sortType, boolean asc) {
        if (sortType == null) {
            return asc ? artist.artistId.asc(): artist.artistId.desc();
        }

        return switch (sortType) {
            case LIKE -> asc ? artist.likeCount.asc() : artist.likeCount.desc();
            case NAME -> asc ? artist.artistName.asc() : artist.artistName.desc();
        };
    }

    /**
     * id 정렬 - 항상 ASC로 고정
     */
    private OrderSpecifier<Long> idOrder(boolean asc) {
        return asc ? artist.artistId.asc(): artist.artistId.desc();
    }
}
