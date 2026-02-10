package com.example.peachmusic.domain.artistlike.repository;

import com.example.peachmusic.domain.artistlike.dto.response.ArtistLikedItemResponseDto;

import java.util.List;

public interface ArtistLikeCustomRepository {

    List<ArtistLikedItemResponseDto> findMyLikedArtistWithCursor(Long userId, Long lastLikeId, int size);
}
