package com.example.peachmusic.domain.album.repository;

import com.example.peachmusic.domain.album.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    Optional<Album> findByAlbumIdAndIsDeletedFalse(Long albumId);

}
