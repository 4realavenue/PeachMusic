package com.example.peachmusic.domain.songlike.repository;

import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.songlike.entity.SongLike;
import com.example.peachmusic.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SongLikeRepository extends JpaRepository<SongLike, Long> {

    boolean existsSongLikeByUserAndSong(User user, Song song);

    SongLike deleteSongLikeByUserAndSong(User user, Song song);


    @Query("""
        select songLike.song.songId
          from SongLike songLike
         where songLike.user.userId = :userId
    """)
    List<Long> findSongsLikedByUser(Long userId);
}
