package com.example.peachmusic.domain.openapi.jamendo.jdbc.row;

public record ArtistAlbumRow(
        Long jamendoArtistId,
        Long jamendoAlbumId
) {}