package com.example.peachmusic.domain.playlist.model;

import com.example.peachmusic.domain.playlist.entity.Playlist;
import com.example.peachmusic.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class PlaylistDto {

    private final Long playlistId;
    private final Long userId;
    private final String playlistName;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public static PlaylistDto from(Playlist playlist) {
        return new PlaylistDto(playlist.getPlaylistId(), playlist.getUser().getUserId(), playlist.getPlaylistName(), playlist.getCreatedAt(), playlist.getModifiedAt());
    }
}
