package com.example.peachmusic.domain.albumLike.repository;

import com.example.peachmusic.domain.albumLike.entity.AlbumLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumLikeRepository extends JpaRepository<AlbumLike, Long> {
}
