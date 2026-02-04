package com.example.peachmusic.domain.songlike.repository;

import com.example.peachmusic.domain.songlike.repository.row.SongLikeRow;

import java.util.List;

public interface SongLikeCustomRepository {

    List<SongLikeRow> findMyLikedSongWithCursor(Long userId, Long cursorLikeId, Integer size);
}
