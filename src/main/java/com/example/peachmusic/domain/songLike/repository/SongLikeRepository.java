package com.example.peachmusic.domain.songLike.repository;

import com.example.peachmusic.domain.songLike.entity.SongLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongLikeRepository extends JpaRepository<SongLike, Long> {
}
