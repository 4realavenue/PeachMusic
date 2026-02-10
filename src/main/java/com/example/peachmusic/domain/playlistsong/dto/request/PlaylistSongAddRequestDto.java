package com.example.peachmusic.domain.playlistsong.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Getter
@NoArgsConstructor
public class PlaylistSongAddRequestDto {

    private Set<Long> songIdSet;
}
