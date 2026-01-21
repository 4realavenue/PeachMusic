package com.example.peachmusic.domain.playlist.model.response;

import com.example.peachmusic.domain.playlist.entity.Playlist;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class PlaylistGetListResponseDto {

    private final Long playlistId;
    private final String playlistName;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public static PlaylistGetListResponseDto from(Playlist playlist) {
        return new PlaylistGetListResponseDto(playlist.getPlaylistId(), playlist.getPlaylistName(), playlist.getCreatedAt(), playlist.getModifiedAt());
    }
}

