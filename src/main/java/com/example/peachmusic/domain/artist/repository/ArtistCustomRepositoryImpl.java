package com.example.peachmusic.domain.artist.repository;

import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.domain.artist.model.response.ArtistSearchResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import java.util.List;
import static com.example.peachmusic.common.enums.UserRole.USER;
import static com.example.peachmusic.domain.artist.entity.QArtist.artist;

public class ArtistCustomRepositoryImpl implements ArtistCustomRepository {

    private final JPAQueryFactory queryFactory;

    public ArtistCustomRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 검색 - 자세히 보기
     * @param word 검색어
     * @param pageable 페이징 정보
     * @return 페이징 처리된 아티스트 검색 결과
     */
    @Override
    public Page<ArtistSearchResponse> findArtistPageByWord(String word, Pageable pageable, UserRole role) {

        List<ArtistSearchResponse> content = baseQuery(word, role)
                .offset(pageable.getOffset()) // 시작 위치
                .limit(pageable.getPageSize()) // 개수
                .fetch();

        Long total = queryFactory
                .select(artist.count())
                .from(artist)
                .where(searchCondition(word, role))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    /**
     * 검색 - 미리보기
     * @param word 검색어
     * @param limit 검색 개수
     * @return 아티스트 검색 미리보기
     */
    @Override
    public List<ArtistSearchResponse> findArtistListByWord(String word, int limit) {
        return baseQuery(word, USER).limit(limit).fetch();
    }

    /**
     * 기본 쿼리
     */
    private JPAQuery<ArtistSearchResponse> baseQuery(String word, UserRole role) {

        return queryFactory
                .select(Projections.constructor(ArtistSearchResponse.class, artist.artistId, artist.artistName, artist.likeCount))
                .from(artist)
                .where(searchCondition(word, role));
    }

    /**
     * 검색 조건
     */
    private BooleanExpression searchCondition(String word, UserRole role) {
        if (role.equals(USER)) {
            return artistNameEquals(word).and(isActive());
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
}
