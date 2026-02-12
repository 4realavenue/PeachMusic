package com.example.peachmusic.domain.playlist.dto.response;

import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.playlist.entity.Playlist;
import com.example.peachmusic.domain.playlistsong.entity.PlaylistSong;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class PlaylistGetSongResponseDto {

    private final Long playlistId;
    private final String playlistName;
    private final String playlistImage;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
    private final List<SongResponseDto> songList;

    public static PlaylistGetSongResponseDto from(Playlist playlist, List<SongResponseDto> playlistSongList) {
        return new PlaylistGetSongResponseDto(playlist.getPlaylistId(), playlist.getPlaylistName(), playlist.getPlaylistImage(), playlist.getCreatedAt(), playlist.getModifiedAt(), playlistSongList);
    }

    @Getter
    @RequiredArgsConstructor
    public static class SongResponseDto {

        private final Long playlistSongId;

        private final String albumName;
        private final String albumImage;

        private final String artistName;

        private final Long songId;
        private final String name;
        private final Long duration;
        private final Long likeCount;
        private final boolean isLiked;

        public static SongResponseDto from(PlaylistSong playlistSong, Album album, String artistName, boolean isLiked) {
            return new SongResponseDto(playlistSong.getPlaylistSongId(), album.getAlbumName(), album.getAlbumImage(), artistName, playlistSong.getSong().getSongId(), playlistSong.getSong().getName(), playlistSong.getSong().getDuration(), playlistSong.getSong().getLikeCount(), isLiked);
        }
    }
}
