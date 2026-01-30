package com.example.peachmusic.domain.albumlike.repository;

import com.example.peachmusic.domain.albumlike.entity.AlbumLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlbumLikeRepository extends JpaRepository<AlbumLike, Long> {

    boolean existsByAlbum_AlbumIdAndUser_UserId(Long albumId, Long userId);

    @Modifying
    @Query("""
        delete from AlbumLike al
        where al.album.albumId = :albumId
        and al.user.userId = :userId
        """)
    int deleteByAlbumIdAndUserId(@Param("albumId") Long albumId, @Param("userId") Long userId);

    @Modifying
    @Query(value = "INSERT IGNORE INTO album_likes (user_id, album_id) VALUES (:userId, :albumId)", nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("albumId") Long albumId);
}
