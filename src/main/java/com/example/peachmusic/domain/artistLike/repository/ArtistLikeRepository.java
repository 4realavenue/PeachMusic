package com.example.peachmusic.domain.artistLike.repository;

import com.example.peachmusic.domain.albumLike.entity.AlbumLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistLikeRepository extends JpaRepository<AlbumLike, Long> {
}
