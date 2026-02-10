package com.example.peachmusic.domain.songlike.repository;

import com.example.peachmusic.domain.songlike.dto.response.SongLikedItemResponseDto;

import java.util.List;

public interface SongLikeCustomRepository {

    List<SongLikedItemResponseDto> findMyLikedSongWithCursor(Long userId, Long lastLikeId, int size);
}
