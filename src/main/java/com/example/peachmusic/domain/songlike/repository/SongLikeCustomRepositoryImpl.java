package com.example.peachmusic.domain.songlike.repository;

import com.example.peachmusic.domain.songlike.repository.row.SongLikeRow;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.List;

import static com.example.peachmusic.domain.songlike.entity.QSongLike.songLike;

public class SongLikeCustomRepositoryImpl implements SongLikeCustomRepository {

    private final JPAQueryFactory queryFactory;

    public SongLikeCustomRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<SongLikeRow> findMyLikedSongWithCursor(Long userId, Long lastId, Integer size) {
        return queryFactory
                .select(Projections.constructor(SongLikeRow.class, songLike.songLikeId, songLike.song.songId, songLike.song.name, songLike.song.likeCount))
                .from(songLike)
                .where(songLike.user.userId.eq(userId), songLike.song.isDeleted.isFalse(), songLike.song.streamingStatus.isTrue(), lastIdCondition(lastId))
                .orderBy(songLike.songLikeId.desc())
                .limit(size + 1)
                .fetch();

    }

    private BooleanExpression lastIdCondition(Long lastId) {
        if (lastId == null) {
            return null;
        }
        return songLike.songLikeId.lt(lastId);
    }
}
