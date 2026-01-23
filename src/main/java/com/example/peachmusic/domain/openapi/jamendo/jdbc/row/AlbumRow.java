package com.example.peachmusic.domain.openapi.jamendo.jdbc.row;

import java.time.LocalDate;

public record AlbumRow(
        Long jamendoAlbumId,
        String albumName,
        LocalDate albumReleaseDate,
        String albumImage
) {}
