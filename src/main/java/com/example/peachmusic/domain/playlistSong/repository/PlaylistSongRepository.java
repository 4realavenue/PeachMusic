package com.example.peachmusic.domain.playlistSong.repository;

import com.example.peachmusic.domain.playlistSong.entity.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {
}
