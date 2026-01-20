package com.example.peachmusic.domain.playlistSong.model.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class PlaylistSongAddResponseDto {

    private final Long playlistId;
    private final List<Long> addedSongIds;
    private final Integer addedCount;

    public static PlaylistSongAddResponseDto from(Long playlistId, List<Long> addedSongIds, Integer addedCount) {
        return new PlaylistSongAddResponseDto(playlistId, addedSongIds, addedCount);
    }

}
