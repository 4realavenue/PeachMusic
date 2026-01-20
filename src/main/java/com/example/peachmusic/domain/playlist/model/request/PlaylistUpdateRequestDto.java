package com.example.peachmusic.domain.playlist.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PlaylistUpdateRequestDto {

    @NotBlank
    private final String playlistName;

}
