package com.example.peachmusic.domain.playlistSong.repository;

import com.example.peachmusic.domain.playlist.entity.Playlist;
import com.example.peachmusic.domain.playlistSong.entity.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {

    boolean existsByPlaylist_PlaylistIdAndSong_SongId(Long playlistId, Long songId);

    List<PlaylistSong> findAllByPlaylist(Playlist playlist);

    void deleteAllByPlaylist(Playlist playlist);

    void deletePlaylistSongByPlaylist_PlaylistIdAndSong_SongId(Long playlistId, Long songId);

}
