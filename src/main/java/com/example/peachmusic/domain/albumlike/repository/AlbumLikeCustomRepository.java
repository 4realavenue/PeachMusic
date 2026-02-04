package com.example.peachmusic.domain.albumlike.repository;

import com.example.peachmusic.domain.albumlike.dto.response.AlbumLikedItemDto;

import java.util.List;

public interface AlbumLikeCustomRepository {

    List<AlbumLikedItemDto> findMyLikedAlbumWithCursor(Long userId, Long lastLikeId, int size);
}
