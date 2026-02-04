package com.example.peachmusic.domain.albumlike.repository;

import com.example.peachmusic.domain.albumlike.repository.row.AlbumLikeRow;

import java.util.List;

public interface AlbumLikeCustomRepository {

    List<AlbumLikeRow> findMyLikedAlbumWithCursor(Long userId, Long cursorLikeId, Integer size);
}
