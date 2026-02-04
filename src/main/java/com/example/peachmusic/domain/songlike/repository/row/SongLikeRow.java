package com.example.peachmusic.domain.songlike.repository.row;

public record SongLikeRow(
        Long songLikeId,
        Long songId,
        String name,
        String audio,
        Long likeCount
) {}
