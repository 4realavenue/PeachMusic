package com.example.peachmusic.domain.playlist.model.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PlaylistUpdateRequestDto {

    private final String playlistName;

}
