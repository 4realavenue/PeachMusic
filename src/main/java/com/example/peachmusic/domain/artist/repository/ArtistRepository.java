package com.example.peachmusic.domain.artist.repository;

import com.example.peachmusic.domain.artist.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    // 활성 상태(isDeleted=false)로 동일한 아티스트 이름이 존재하는지 여부 확인
    boolean existsByArtistNameAndIsDeletedFalse(String artistName);

    // 비활성 상태(isDeleted=true)로 동일한 아티스트 이름의 아티스트 조회
    Optional<Artist> findByArtistNameAndIsDeletedTrue(String artistName);
}
