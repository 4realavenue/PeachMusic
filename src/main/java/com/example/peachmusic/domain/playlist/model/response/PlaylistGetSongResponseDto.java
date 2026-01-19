package com.example.peachmusic.domain.playlist.model.response;

import com.example.peachmusic.domain.playlist.entity.Playlist;
import com.example.peachmusic.domain.playlistSong.entity.PlaylistSong;
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

    public static PlaylistGetSongResponseDto from(Playlist playlist, List<PlaylistSongResponseDto> playlistSongs) {
        return new PlaylistGetSongResponseDto(playlist.getPlaylistId(), playlist.getPlaylistName(), playlist.getCreatedAt(), playlist.getModifiedAt(), playlistSongs);
    }

    @Getter
    @RequiredArgsConstructor
    public static class PlaylistSongResponseDto {

        private final Long playlistSongId;
        private final Long songId;
        private final String name;
        private final Long duration;
        private final Long likeCount;

        public static PlaylistSongResponseDto from(PlaylistSong playlistSong) {
            return new PlaylistSongResponseDto(playlistSong.getPlaylistSongId(), playlistSong.getSong().getSongId(), playlistSong.getSong().getName(), playlistSong.getSong().getDuration(), playlistSong.getSong().getLikeCount());
        }
    }

}
