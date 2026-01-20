package com.example.peachmusic.domain.artist.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArtistUpdateRequestDto {

    @NotBlank(message = "아티스트명은 필수입니다.")
    private String artistName;
}
