package com.example.peachmusic.domain.artistAlbum.repository;

import com.example.peachmusic.domain.artistAlbum.entity.ArtistAlbum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtistAlbumRepository extends JpaRepository<ArtistAlbum, Long> {

    // 앨범에 참여한 아티스트 조회
    List<ArtistAlbum> findAllByAlbum_AlbumId(Long albumId);
}
