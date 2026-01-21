package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.song.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SongRepository extends JpaRepository<Song, Long>, SongCustomRepository {

    Optional<Song> findBySongIdAndIsDeletedFalse(Long songId);

    Page<Song> findAll(Pageable pageable);

    boolean existsSongByAlbumAndPosition(Album album, Long position);

    boolean existsSongByAudio(String audioUrl);

    // (활성) 앨범 음원 목록 조회
    List<Song> findAllByAlbum_AlbumIdAndIsDeletedFalse(Long albumId);

    // (비활성) 앨범 음원 목록 조회
    List<Song> findAllByAlbum_AlbumIdAndIsDeletedTrue(Long albumId);

    // (활성) 여러 앨범 음원 조회
    List<Song> findAllByAlbum_AlbumIdInAndIsDeletedFalse(List<Long> albumIds);

    // (비활성) 여러 앨범 음원 조회
    List<Song> findAllByAlbum_AlbumIdInAndIsDeletedTrue(List<Long> albumIds);

    boolean existsByJamendoSongId(String jamendoSongId);

    @Query("""
            select s.songId from Song s
            where s.songId in (:songIdList)
            """)
    List<Long> findSongIdListBySongIdList(List<Long> songIdList);

    boolean existsByAudioAndSongIdNot(String audio, Long songId);


    @Query("""
            select s.album.albumId from Song s
            """)
    Long findSongs_AlbumIdBySongId(Song song);
}
