package com.example.peachmusic.domain.artistlike.repository.row;

public record ArtistLikeRow(
        Long artistLikeId,
        Long artistId,
        String artistName,
        String profileImage,
        Long likeCount
) {}


