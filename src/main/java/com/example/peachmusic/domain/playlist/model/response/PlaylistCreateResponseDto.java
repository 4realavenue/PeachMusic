package com.example.peachmusic.domain.playlist.model.response;

import com.example.peachmusic.domain.playlist.model.PlaylistDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class PlaylistCreateResponseDto {

    private final Long playlistId;
    private final Long userId;
    private final String playlistName;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public static PlaylistCreateResponseDto from(PlaylistDto playlistDto) {
        return new PlaylistCreateResponseDto(playlistDto.getPlaylistId(), playlistDto.getUserId(), playlistDto.getPlaylistName(), playlistDto.getCreatedAt(), playlistDto.getModifiedAt());
    }
}
