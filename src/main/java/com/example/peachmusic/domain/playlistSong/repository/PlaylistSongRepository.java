package com.example.peachmusic.domain.playlistSong.repository;

import com.example.peachmusic.domain.playlistSong.entity.PlaylistSong;
import com.example.peachmusic.domain.song.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {

    boolean existsByPlaylist_PlaylistIdAndSong_SongId(Long playlistId,Long songId);
}
