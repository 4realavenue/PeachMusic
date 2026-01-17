package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.domain.song.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {


    Page<Song> findAllByIsDeletedFalse (Pageable pageable);

}
