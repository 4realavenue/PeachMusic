package com.example.peachmusic.domain.user.repository;

import com.example.peachmusic.domain.user.dto.response.admin.UserAdminGetResponseDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import static com.example.peachmusic.domain.user.entity.QUser.user;

public class UserCustomRepositoryImpl implements UserCustomRepository{

    private final JPAQueryFactory queryFactory;

    public UserCustomRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 사용자 조회
     */
    @Override
    public List<UserAdminGetResponseDto> findUserKeysetPageByWord(String word, int size, Long lastId) {
        return queryFactory
                .select(Projections.constructor(UserAdminGetResponseDto.class, user.userId, user.role, user.name, user.nickname, user.email, user.createdAt, user.modifiedAt, user.isDeleted))
                .from(user)
                .where(searchCondition(word), keysetCondition(lastId))
                .orderBy(user.userId.asc())
                .limit(size+1) // 요청한 사이즈보다 하나 더 많은 데이터를 조회
                .fetch();
    }

    /**
     * 검색 조건: 닉네임
     */
    private BooleanExpression searchCondition(String word) {
        return word != null ? user.nickname.eq(word) : null;
    }

    /**
     * Keyset 조건: 사용자 id
     */
    private BooleanExpression keysetCondition(Long lastId) {
        return lastId != null ? user.userId.gt(lastId) : null;
    }
}
