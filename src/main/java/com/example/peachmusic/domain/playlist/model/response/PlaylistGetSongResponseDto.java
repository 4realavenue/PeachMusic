package com.example.peachmusic.domain.playlist.model.response;

import com.example.peachmusic.domain.playlist.model.PlaylistDto;
import com.example.peachmusic.domain.playlistSong.model.response.PlaylistSongResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class PlaylistGetSongResponseDto{

    private final Long playlistId;
    private final String playlistName;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
    private final List<PlaylistSongResponseDto> songs;

    public static PlaylistGetSongResponseDto from(PlaylistDto playlistDto, List<PlaylistSongResponseDto> playlistSongs) {
        return new PlaylistGetSongResponseDto(playlistDto.getPlaylistId(), playlistDto.getPlaylistName(), playlistDto.getCreatedAt(), playlistDto.getModifiedAt(), playlistSongs);
    }

}
