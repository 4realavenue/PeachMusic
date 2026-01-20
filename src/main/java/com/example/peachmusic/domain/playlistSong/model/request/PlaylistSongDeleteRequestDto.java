package com.example.peachmusic.domain.playlistSong.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PlaylistSongDeleteRequestDto {

    private List<Long> songIds;
}
