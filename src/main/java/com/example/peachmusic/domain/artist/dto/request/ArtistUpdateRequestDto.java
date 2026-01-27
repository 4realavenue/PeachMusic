package com.example.peachmusic.domain.artist.dto.request;

import com.example.peachmusic.common.enums.ArtistType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ArtistUpdateRequestDto {

    private String artistName;

    private String country;

    private ArtistType artistType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate debutDate;

    @Size(max = 500)
    private String bio;
}
