package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.song.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    Optional<Song> findBySongIdAndIsDeletedFalse(Long songId);

    Page<Song> findAll(Pageable pageable);

    boolean existsSongByAlbumAndPosition(Album album, Long position);


    // (활성) 앨범 음원 목록 조회 (position ASC)
    List<Song> findAllByAlbum_AlbumIdAndIsDeletedFalseOrderByPositionAsc(Long albumId);

    // (비활성) 앨범 음원 목록 조회 (position ASC)
    List<Song> findAllByAlbum_AlbumIdAndIsDeletedTrueOrderByPositionAsc(Long albumId);

    // (활성) 여러 앨범 음원 조회
    List<Song> findAllByAlbum_AlbumIdInAndIsDeletedFalseOrderByPositionAsc(List<Long> albumIds);

    // (비활성) 여러 앨범 음원 조회
    List<Song> findAllByAlbum_AlbumIdInAndIsDeletedTrueOrderByPositionAsc(List<Long> albumIds);
}
