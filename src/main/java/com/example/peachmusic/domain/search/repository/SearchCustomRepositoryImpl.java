package com.example.peachmusic.domain.search.repository;

import com.example.peachmusic.domain.search.model.SearchPopularResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;
import static com.example.peachmusic.domain.search.entity.QSearch.search;

public class SearchCustomRepositoryImpl implements SearchCustomRepository {

    private static final int POPULAR_KEYWORD_LIMIT = 10;

    private final JPAQueryFactory queryFactory;

    public SearchCustomRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 인기 검색어 조회
     */
    @Override
    public List<SearchPopularResponse> findPopularKeyword() {

        NumberExpression<Integer> rank = Expressions.numberTemplate(Integer.class, "ROW_NUMBER() OVER (ORDER BY {0} DESC)", search.count);

        return queryFactory
                .select(Projections.constructor(SearchPopularResponse.class, rank, search.word, search.count))
                .from(search)
                .where(search.searchDate.goe(LocalDate.now().minusWeeks(1))) // 일주일 동안의 검색어
                .orderBy(search.count.desc()) // 검색 횟수 내림차순
                .limit(POPULAR_KEYWORD_LIMIT)
                .fetch();
    }
}
