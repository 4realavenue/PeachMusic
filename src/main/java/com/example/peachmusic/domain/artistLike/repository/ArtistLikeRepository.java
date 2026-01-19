package com.example.peachmusic.domain.artistLike.repository;

import com.example.peachmusic.domain.artistLike.entity.ArtistLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistLikeRepository extends JpaRepository<ArtistLike, Long> {

    /**
     * 아티스트 좋아요 관련 조회/삭제/집계 쿼리 메서드
     */
    boolean existsByArtist_ArtistIdAndUser_UserId(Long artistId, Long userId);

    void deleteByArtist_ArtistIdAndUser_UserId(Long artistId, Long userId);

    long countByArtist_ArtistId(Long artistId);
}
