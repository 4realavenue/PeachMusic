package com.example.peachmusic.domain.artistalbum.repository;

import com.example.peachmusic.domain.album.dto.response.ArtistSummaryDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.List;

import static com.example.peachmusic.domain.artist.entity.QArtist.artist;
import static com.example.peachmusic.domain.artistalbum.entity.QArtistAlbum.artistAlbum;

public class ArtistAlbumCustomRepositoryImpl implements ArtistAlbumCustomRepository {

    private final JPAQueryFactory queryFactory;

    public ArtistAlbumCustomRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<ArtistSummaryDto> findArtistSummaryListByAlbumId(Long albumId) {
        return queryFactory
                .select(Projections.constructor(ArtistSummaryDto.class, artist.artistId, artist.artistName))
                .from(artistAlbum)
                .join(artistAlbum.artist, artist)
                .where(artistAlbum.album.albumId.eq(albumId), artist.isDeleted.isFalse())
                .orderBy(artistAlbum.artistAlbumId.asc())
                .fetch();
    }
}
