package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.domain.song.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    // (활성) 앨범 음원 목록 조회 (position ASC)
    List<Song> findAllByAlbum_AlbumIdAndIsDeletedFalseOrderByPositionAsc(Long albumId);

    // (비활성) 앨범 음원 목록 조회 (position ASC)
    List<Song> findAllByAlbum_AlbumIdAndIsDeletedTrueOrderByPositionAsc(Long albumId);
}
