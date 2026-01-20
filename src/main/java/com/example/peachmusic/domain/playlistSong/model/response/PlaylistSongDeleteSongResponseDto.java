package com.example.peachmusic.domain.playlistSong.model.response;

import com.example.peachmusic.domain.playlist.entity.Playlist;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class PlaylistSongDeleteSongResponseDto {

    private final Long playlistId;
    private final List<Long> deletedSongIds;

    public static PlaylistSongDeleteSongResponseDto from(Playlist playlist, List<Long> deletedSongIds) {
        return new PlaylistSongDeleteSongResponseDto(playlist.getPlaylistId(), deletedSongIds);
    }
}
