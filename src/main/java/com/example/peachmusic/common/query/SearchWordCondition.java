package com.example.peachmusic.common.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SearchWordCondition {

    /**
     * 검색 조건
     * @param path 문자열 필드 (Q클래스)
     * @param word 검색어
     * @return 검색어가 특정 필드 안에 포함되어 있는지
     */
    public static BooleanExpression wordMatch(StringPath path, String word) {
        return Expressions.booleanTemplate("function('match_against', {0}, {1}) > 0", path, Expressions.constant(word));
    }
}
