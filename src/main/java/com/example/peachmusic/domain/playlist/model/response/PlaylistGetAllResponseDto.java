package com.example.peachmusic.domain.playlist.model.response;

import com.example.peachmusic.domain.playlist.entity.Playlist;
import com.example.peachmusic.domain.playlist.model.PlaylistDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class PlaylistGetAllResponseDto {

    private final List<PlaylistResponseDto> playlistList;

    public static PlaylistGetAllResponseDto from(List<PlaylistResponseDto> playlistList) {
        return new PlaylistGetAllResponseDto(playlistList);
    }

    @Getter
    @RequiredArgsConstructor
    public static class PlaylistResponseDto {

        private final Long playlistId;
        private final String playlistName;
        private final LocalDateTime createdAt;
        private final LocalDateTime modifiedAt;

        public static PlaylistResponseDto from(PlaylistDto playlistDto) {
            return new PlaylistResponseDto(playlistDto.getPlaylistId(), playlistDto.getPlaylistName(), playlistDto.getCreatedAt(), playlistDto.getModifiedAt());
        }

    }
}
