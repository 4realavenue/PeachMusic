package com.example.peachmusic.domain.artist.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArtistCreateRequestDto {

    @NotBlank(message = "아티스트명은 필수입니다.")
    private String artistName;
}
