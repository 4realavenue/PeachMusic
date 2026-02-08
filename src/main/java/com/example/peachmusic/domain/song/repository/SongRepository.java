package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.song.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import java.util.List;
import java.util.Set;

public interface SongRepository extends JpaRepository<Song, Long>, SongCustomRepository, RecommendationRepository {

    Optional<Song> findBySongIdAndIsDeletedFalse(Long songId);

    Page<Song> findAll(Pageable pageable);

    boolean existsSongByAlbumAndPosition(Album album, Long position);

    boolean existsSongByAudio(String audioUrl);

    // 앨범 음원 목록 조회
    List<Song> findAllByAlbum_AlbumIdAndIsDeletedFalse(Long albumId);
    List<Song> findAllByAlbum_AlbumIdAndIsDeletedTrue(Long albumId);

    List<Song> findAllByAlbum_AlbumIdAndIsDeletedFalseAndStreamingStatusTrue(Long albumId);

    @Query("""
            select s.songId from Song s
            where s.songId in (:songIdList)
            """)
    List<Long> findSongIdListBySongIdList(List<Long> songIdList);

    boolean existsByAudioAndSongIdNot(String audio, Long songId);

    @Query("""
            select s.album.albumId from Song s
            where s = :song
            """)
    Long findSongs_AlbumIdBySongId(Song song);

    @Query("select s.jamendoSongId from Song s where s.jamendoSongId is not null")
    Set<Long> findJamendoSongIdList();

    boolean existsByAlbumAndName(Album album, String name);

    boolean existsSongByAlbumAndPositionAndSongIdNot(Album album, Long position, Long songId);

    @Query("""
        select s.likeCount
        from Song s
        where s.songId = :songId
        and s.isDeleted = false
        """)
    Long findLikeCountBySongId(@Param("songId") Long songId);

    @Modifying
    @Query("update Song s set s.likeCount = s.likeCount + 1 where s.songId = :songId")
    void incrementLikeCount(@Param("songId") Long songId);

    @Modifying
    @Query("update Song s set s.likeCount = s.likeCount - 1 where s.songId = :songId and s.likeCount > 0")
    void decrementLikeCount(@Param("songId") Long songId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Song s
        set s.isDeleted = true
        where s.isDeleted = false
        and s.album.albumId in :albumIdList
        """)
    void softDeleteByAlbumIdList(@Param("albumIdList") List<Long> albumIdList);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Song s
        set s.isDeleted = false
        where s.isDeleted = true
        and s.album.albumId in :albumIdList
        """)
    void restoreByAlbumIdList(@Param(("albumIdList")) List<Long> albumIdList);
}