package com.example.peachmusic.domain.albumlike.repository;

import com.example.peachmusic.domain.albumlike.dto.response.AlbumLikedItemResponseDto;

import java.util.List;

public interface AlbumLikeCustomRepository {

    List<AlbumLikedItemResponseDto> findMyLikedAlbumWithCursor(Long userId, Long lastLikeId, int size);
}
