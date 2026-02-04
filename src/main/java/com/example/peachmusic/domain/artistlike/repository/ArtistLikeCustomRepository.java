package com.example.peachmusic.domain.artistlike.repository;

import com.example.peachmusic.domain.artistlike.dto.response.ArtistLikedItemDto;

import java.util.List;

public interface ArtistLikeCustomRepository {

    List<ArtistLikedItemDto> findMyLikedArtistWithCursor(Long userId, Long lastLikeId, int size);
}
