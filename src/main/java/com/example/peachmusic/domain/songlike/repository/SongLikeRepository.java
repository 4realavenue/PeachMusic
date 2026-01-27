package com.example.peachmusic.domain.songlike.repository;

import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.songlike.entity.SongLike;
import com.example.peachmusic.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongLikeRepository extends JpaRepository<SongLike, Long> {

    boolean existsSongLikeByUserAndSong(User user, Song song);

    SongLike deleteSongLikeByUserAndSong(User user, Song song);

}
