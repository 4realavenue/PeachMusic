package com.example.peachmusic.domain.playlist.dto.response;

import com.example.peachmusic.domain.playlist.entity.Playlist;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class PlaylistCreateResponseDto {

    private final Long playlistId;
    private final Long userId;
    private final String playlistName;
    private final String playlistImage;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public static PlaylistCreateResponseDto from(Playlist playlist) {
        return new PlaylistCreateResponseDto(playlist.getPlaylistId(), playlist.getUser().getUserId(), playlist.getPlaylistName(), playlist.getPlaylistImage(), playlist.getCreatedAt(), playlist.getModifiedAt());
    }
}
