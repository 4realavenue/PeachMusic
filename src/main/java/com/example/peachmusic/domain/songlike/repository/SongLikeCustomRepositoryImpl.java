package com.example.peachmusic.domain.songlike.repository;

import com.example.peachmusic.domain.songlike.dto.response.SongLikedItemResponseDto;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.List;

import static com.example.peachmusic.domain.artist.entity.QArtist.artist;
import static com.example.peachmusic.domain.artistalbum.entity.QArtistAlbum.artistAlbum;
import static com.example.peachmusic.domain.songlike.entity.QSongLike.songLike;

public class SongLikeCustomRepositoryImpl implements SongLikeCustomRepository {

    private final JPAQueryFactory queryFactory;

    public SongLikeCustomRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<SongLikedItemResponseDto> findMyLikedSongWithCursor(Long userId, Long lastLikeId, int size) {
        return queryFactory
                .select(Projections.constructor(SongLikedItemResponseDto.class, songLike.songLikeId, representativeArtistName(), songLike.song.album.albumName, songLike.song.album.albumImage, songLike.song.songId, songLike.song.name, songLike.song.audio, songLike.song.likeCount))
                .from(songLike)
                .where(songLike.user.userId.eq(userId), songLike.song.isDeleted.isFalse(), songLike.song.streamingStatus.isTrue(), lastLikeIdCondition(lastLikeId))
                .orderBy(songLike.songLikeId.desc())
                .limit(size + 1)
                .fetch();

    }

    private BooleanExpression lastLikeIdCondition(Long lastLikeId) {
        if (lastLikeId == null) {
            return null;
        }
        return songLike.songLikeId.lt(lastLikeId);
    }

    private Expression<String> representativeArtistName() {
        return JPAExpressions
                .select(artist.artistName.min()) // 리스트용으로 대표 아티스트 1명만 노출
                .from(artistAlbum)
                .join(artistAlbum.artist, artist)
                .where(artistAlbum.album.eq(songLike.song.album));
    }
}
