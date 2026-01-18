package com.example.peachmusic.domain.playlist.model.response;

import com.example.peachmusic.domain.playlist.model.PlaylistDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class PlaylistUpdateResponseDto {

    private final Long playlistId;
    private final String playlistName;
    private final LocalDateTime modifiedAt;

    public static PlaylistUpdateResponseDto from(PlaylistDto playlistDto) {
        return new PlaylistUpdateResponseDto(playlistDto.getPlaylistId(), playlistDto.getPlaylistName(), playlistDto.getModifiedAt());
    }
}
