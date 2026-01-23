package com.example.peachmusic.domain.openapi.jamendo.jdbc.row;

public record ArtistSongRow(
        Long jamendoArtistId,
        Long jamendoSongId
) {}
