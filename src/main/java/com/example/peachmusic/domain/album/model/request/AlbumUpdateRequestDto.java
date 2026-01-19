package com.example.peachmusic.domain.album.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlbumUpdateRequestDto {

    private String albumName;
    private LocalDate albumReleaseDate;
    private String albumImage;
}
