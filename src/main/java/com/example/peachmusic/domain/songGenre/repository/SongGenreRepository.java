package com.example.peachmusic.domain.songGenre.repository;

import com.example.peachmusic.domain.songGenre.entity.SongGenre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongGenreRepository extends JpaRepository<SongGenre, Long> {
}
