package com.example.peachmusic.domain.playlist.repository;

import com.example.peachmusic.domain.playlist.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
}
