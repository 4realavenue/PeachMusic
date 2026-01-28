package com.example.peachmusic.domain.songlike.repository;

import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.songlike.entity.SongLike;
import com.example.peachmusic.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SongLikeRepository extends JpaRepository<SongLike, Long> {

    boolean existsSongLikeByUserAndSong(User user, Song song);

    @Modifying
    @Query("""
        delete from SongLike sl
        where sl.song.songId = :songId
        and sl.user.userId = :userId
        """)
    int deleteBySongIdAndUserId(@Param("songId") Long songId, @Param("userId") Long userId);

    @Modifying
    @Query(value = "INSERT IGNORE INTO song_likes (user_id, song_id) VALUES (:userId, :songId)", nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("songId") Long songId);
}
