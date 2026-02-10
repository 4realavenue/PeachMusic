package com.example.peachmusic.domain.albumlike.repository;

import com.example.peachmusic.domain.albumlike.dto.response.AlbumLikedItemResponseDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.List;

import static com.example.peachmusic.domain.albumlike.entity.QAlbumLike.albumLike;

public class AlbumLikeCustomRepositoryImpl implements AlbumLikeCustomRepository {

    private final JPAQueryFactory queryFactory;

    public AlbumLikeCustomRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 사용자가 좋아요한 앨범 목록을 Keyset 방식으로 조회
     * 정렬 기준:
     * - albumLikeId DESC
     * Keyset 조건:
     * - 마지막 조회된 lastId(albumLikeId)보다 작은 데이터 조회
     * size + 1 조회 후 다음 페이지 존재 여부(hasNext) 판단
     */
    @Override
    public List<AlbumLikedItemResponseDto> findMyLikedAlbumWithCursor(Long userId, Long lastLikeId, int size) {
        return queryFactory
                .select(Projections.constructor(AlbumLikedItemResponseDto.class, albumLike.albumLikeId, albumLike.album.albumId, albumLike.album.albumName, albumLike.album.albumImage, albumLike.album.likeCount))
                .from(albumLike)
                .where(albumLike.user.userId.eq(userId), albumLike.album.isDeleted.isFalse(), lastLikeIdCondition(lastLikeId))
                .orderBy(albumLike.albumLikeId.desc())
                .limit(size + 1)
                .fetch();
    }

    /**
     * 첫 페이지 조회 시 lastId가 없으면
     * Keyset 조건을 적용하지 않기 위해 null 반환
     */
    private BooleanExpression lastLikeIdCondition(Long lastLikeId) {
        if (lastLikeId == null) {
            return null;
        }
        return albumLike.albumLikeId.lt(lastLikeId);
    }
}
