package com.example.peachmusic.domain.songGenre.repository;

import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.songGenre.entity.SongGenre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongGenreRepository extends JpaRepository<SongGenre, Long> {

    List<SongGenre> deleteAllBySong (Song song);

    List<SongGenre> findAllBySong (Song song);

}
