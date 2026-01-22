package com.example.peachmusic.domain.playlist.dto.response;

import com.example.peachmusic.domain.playlist.entity.Playlist;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class PlaylistUpdateResponseDto {

    private final Long playlistId;
    private final String playlistName;
    private final String playlistImage;
    private final LocalDateTime modifiedAt;

    public static PlaylistUpdateResponseDto from(Playlist playlist) {
        return new PlaylistUpdateResponseDto(playlist.getPlaylistId(), playlist.getPlaylistName(), playlist.getPlaylistImage(), playlist.getModifiedAt());
    }
}
