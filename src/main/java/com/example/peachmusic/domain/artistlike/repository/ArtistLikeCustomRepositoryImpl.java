package com.example.peachmusic.domain.artistlike.repository;

import com.example.peachmusic.domain.artistlike.dto.response.ArtistLikedItemResponseDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.List;

import static com.example.peachmusic.domain.artistlike.entity.QArtistLike.artistLike;

public class ArtistLikeCustomRepositoryImpl implements ArtistLikeCustomRepository {

    private final JPAQueryFactory queryFactory;

    public ArtistLikeCustomRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<ArtistLikedItemResponseDto> findMyLikedArtistWithCursor(Long userId, Long lastLikeId, int size) {
        return queryFactory
                .select(Projections.constructor(ArtistLikedItemResponseDto.class, artistLike.artistLikeId, artistLike.artist.artistId, artistLike.artist.artistName, artistLike.artist.profileImage, artistLike.artist.likeCount))
                .from(artistLike)
                .where(artistLike.user.userId.eq(userId), artistLike.artist.isDeleted.isFalse(), lastLikeIdCondition(lastLikeId))
                .orderBy(artistLike.artistLikeId.desc())
                .limit(size + 1)
                .fetch();

    }

    private BooleanExpression lastLikeIdCondition(Long lastLikeId) {
        if (lastLikeId == null) {
            return null;
        }
        return artistLike.artistLikeId.lt(lastLikeId);
    }
}
