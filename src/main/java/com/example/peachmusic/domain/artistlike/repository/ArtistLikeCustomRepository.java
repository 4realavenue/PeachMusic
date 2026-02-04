package com.example.peachmusic.domain.artistlike.repository;

import com.example.peachmusic.domain.artistlike.repository.row.ArtistLikeRow;

import java.util.List;

public interface ArtistLikeCustomRepository {

    List<ArtistLikeRow> findMyLikedArtistWithCursor(Long userId, Long cursorLikeId, Integer size);
}
