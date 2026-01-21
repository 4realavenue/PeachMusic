package com.example.peachmusic.domain.artistSong.repository;

import com.example.peachmusic.domain.artistSong.entity.ArtistSong;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistSongRepository extends JpaRepository<ArtistSong, Long> {
    boolean existsByArtist_ArtistIdAndSong_SongId(Long artistId, Long songId);
}
