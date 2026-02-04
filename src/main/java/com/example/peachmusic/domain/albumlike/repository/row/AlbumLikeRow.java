package com.example.peachmusic.domain.albumlike.repository.row;

public record AlbumLikeRow(
        Long albumLikeId,
        Long albumId,
        String albumName,
        String albumImage,
        Long likeCount
) {}
