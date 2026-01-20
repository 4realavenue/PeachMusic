package com.example.peachmusic.domain.playlist.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlaylistCreateRequestDto {

    @NotBlank
    private String playlistName;

}
