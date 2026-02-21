package com.example.peachmusic.domain.artistlike.repository;

import com.example.peachmusic.domain.artistlike.entity.ArtistLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Set;

public interface ArtistLikeRepository extends JpaRepository<ArtistLike, Long>, ArtistLikeCustomRepository {

    boolean existsByArtist_ArtistIdAndUser_UserId(Long artistId, Long userId);

    @Modifying
    @Query("""
        delete from ArtistLike al
        where al.artist.artistId = :artistId
        and al.user.userId = :userId
        """)
    int deleteByArtistIdAndUserId(@Param("artistId") Long artistId, @Param("userId") Long userId);

    @Modifying
    @Query(value = "INSERT IGNORE INTO artist_likes (user_id, artist_id) VALUES (:userId, :artistId)", nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("artistId") Long artistId);

    @Query("""
            select al.artist.artistId
            from ArtistLike al
            where al.user.userId = :userId
            and al.artist.artistId in :artistIdList
    """)
    Set<Long> findLikedArtistIdList(Long userId, List<Long> artistIdList);
}
