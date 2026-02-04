package com.example.peachmusic.domain.songlike.repository;

import com.example.peachmusic.domain.songlike.dto.response.SongLikedItemDto;

import java.util.List;

public interface SongLikeCustomRepository {

    List<SongLikedItemDto> findMyLikedSongWithCursor(Long userId, Long lastLikeId, int size);
}
