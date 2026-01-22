package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.song.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;
import java.util.Set;

public interface SongRepository extends JpaRepository<Song, Long>, SongCustomRepository {

    Optional<Song> findBySongIdAndIsDeletedFalse(Long songId);

    boolean existsSongByAlbumAndPosition(Album album, Long position);

    // (활성) 앨범 음원 목록 조회
    List<Song> findAllByAlbum_AlbumIdAndIsDeletedFalse(Long albumId);

    // (비활성) 앨범 음원 목록 조회
    List<Song> findAllByAlbum_AlbumIdAndIsDeletedTrue(Long albumId);

    // (활성) 여러 앨범 음원 조회
    List<Song> findAllByAlbum_AlbumIdInAndIsDeletedFalse(List<Long> albumIds);

    // (비활성) 여러 앨범 음원 조회
    List<Song> findAllByAlbum_AlbumIdInAndIsDeletedTrue(List<Long> albumIds);

    @Query("select s.jamendoSongId from Song s where s.jamendoSongId is not null")
    Set<Long> findSongIdSet();
}