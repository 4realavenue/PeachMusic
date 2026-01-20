package com.example.peachmusic.domain.playlist.repository;

import com.example.peachmusic.domain.playlist.entity.Playlist;
import com.example.peachmusic.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    List<Playlist> findAllByUser(User user);
}
