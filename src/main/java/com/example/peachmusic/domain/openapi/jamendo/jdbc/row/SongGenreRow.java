package com.example.peachmusic.domain.openapi.jamendo.jdbc.row;

public record SongGenreRow(
        Long jamendoSongId,
        String genreName
) {}
