package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.domain.song.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<Song, Long> {

    Song findBySongIdAndIsDeletedFalse (Long songId);

    Page<Song> findAllByIsDeletedFalse (Pageable pageable);



}
