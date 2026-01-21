package com.example.peachmusic.domain.searchHistory.repository;

import com.example.peachmusic.domain.searchHistory.dto.SearchPopularResponseDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import static com.example.peachmusic.domain.searchHistory.entity.QSearchHistory.searchHistory;

public class SearchHistoryCustomRepositoryImpl implements SearchHistoryCustomRepository {

    private static final int POPULAR_KEYWORD_LIMIT = 10;

    private final JPAQueryFactory queryFactory;

    public SearchHistoryCustomRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 인기 검색어 조회
     */
    @Override
    public List<SearchPopularResponseDto> findPopularKeyword() {

        NumberExpression<Integer> rank = Expressions.numberTemplate(Integer.class, "ROW_NUMBER() OVER (ORDER BY {0} DESC)", searchHistory.count);

        return queryFactory
                .select(Projections.constructor(SearchPopularResponseDto.class, rank, searchHistory.word, searchHistory.count))
                .from(searchHistory)
                .where(searchHistory.searchDate.goe(LocalDate.now().minusWeeks(1))) // 일주일 동안의 검색어
                .orderBy(searchHistory.count.desc()) // 검색 횟수 내림차순
                .limit(POPULAR_KEYWORD_LIMIT)
                .fetch();
    }
}
