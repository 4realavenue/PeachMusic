package com.example.peachmusic.domain.playlistSong.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PlaylistSongAddRequestDto {

    private List<Long> songIdList;
}
