package com.example.peachmusic.domain.playlist.dto.response;

import com.example.peachmusic.domain.playlist.entity.Playlist;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class PlaylistImageUpdateResponseDto {

    private final Long playlistId;
    private final String playlistName;
    private final String playlistImage;
    private final LocalDateTime modifiedAt;

    public static PlaylistImageUpdateResponseDto from(Playlist playlist) {
        return new PlaylistImageUpdateResponseDto(playlist.getPlaylistId(), playlist.getPlaylistName(), playlist.getPlaylistImage(), playlist.getModifiedAt());
    }
}
