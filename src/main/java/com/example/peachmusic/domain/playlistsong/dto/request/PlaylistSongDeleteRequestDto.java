package com.example.peachmusic.domain.playlistsong.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Getter
@NoArgsConstructor
public class PlaylistSongDeleteRequestDto {

    private Set<Long> songIdSet;
}
