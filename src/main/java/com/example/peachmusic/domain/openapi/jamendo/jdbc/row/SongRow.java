package com.example.peachmusic.domain.openapi.jamendo.jdbc.row;

public record SongRow(
        Long jamendoSongId,
        Long jamendoAlbumId,
        String name,
        Long duration,
        String licenseCcurl,
        Long position,
        String audio,
        String vocalInstrumental,
        String lang,
        String speed,
        String instruments,
        String vartags
) {}
