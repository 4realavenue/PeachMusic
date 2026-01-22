package com.example.peachmusic.domain.albumLike.repository;

import com.example.peachmusic.domain.albumLike.entity.AlbumLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumLikeRepository extends JpaRepository<AlbumLike, Long> {

    /**
     * 앨범 좋아요 관련 조회/삭제 쿼리 메서드
     */
    boolean existsByAlbum_AlbumIdAndUser_UserId(Long albumId, Long userId);

    void deleteByAlbum_AlbumIdAndUser_UserId(Long albumId, Long userId);

    boolean existsByUser_UserIdAndAlbum_AlbumId(Long userId, Long albumId);
}
