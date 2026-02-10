package com.example.peachmusic.domain.openapi.jamendo.jdbc.row;

public record SongProgressingStatusRow(
        Long jamendoSongId,
        String progressingStatus
) {}

