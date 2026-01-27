package com.example.peachmusic.domain.playlistsong.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PlaylistSongDeleteRequestDto {

    private List<Long> songIdList;
}
