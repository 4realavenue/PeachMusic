package com.example.peachmusic.domain.openapi.jamendo.jdbc.row;

public record StreamingJobRow(
        Long jamendoSongId,
        String jobStatus
) {}

