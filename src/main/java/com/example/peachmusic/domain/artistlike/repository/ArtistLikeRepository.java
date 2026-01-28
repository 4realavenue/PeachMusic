package com.example.peachmusic.domain.artistlike.repository;

import com.example.peachmusic.domain.artistlike.entity.ArtistLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArtistLikeRepository extends JpaRepository<ArtistLike, Long> {

    boolean existsByArtist_ArtistIdAndUser_UserId(Long artistId, Long userId);

    @Modifying
    @Query("""
        delete from ArtistLike al
        where al.artist.artistId = :artistId
        and al.user.userId = :userId
        """)
    int deleteByArtistIdAndUserId(@Param("artistId") Long artistId, @Param("userId") Long userId);
}
