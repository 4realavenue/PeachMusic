package com.example.peachmusic.domain.openapi.jamendo.jdbc.row;

public record ArtistRow(
        Long jamendoArtistId,
        String artistName
) {}