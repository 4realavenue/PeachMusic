package com.example.peachmusic.domain.artistsong.repository;

import com.example.peachmusic.domain.artistsong.entity.ArtistSong;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistSongRepository extends JpaRepository<ArtistSong, Long> {
}
