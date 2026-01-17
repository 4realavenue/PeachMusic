package com.example.peachmusic.domain.artist.repository;

import com.example.peachmusic.domain.artist.entity.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    // 활성 상태(isDeleted=false)로 동일한 아티스트 이름이 존재하는지 여부 확인
    boolean existsByArtistNameAndIsDeletedFalse(String artistName);

    // 비활성 상태(isDeleted=true)로 동일한 아티스트 이름의 아티스트 조회
    Optional<Artist> findByArtistNameAndIsDeletedTrue(String artistName);

    // 활성 상태(isDeleted=false)인 아티스트 목록을 페이징 조건에 맞게 조회
    Page<Artist> findAllByIsDeletedFalse(Pageable pageable);

    // 활성 상태(isDeleted=false)인 아티스트 조회
    Optional<Artist> findByArtistIdAndIsDeletedFalse(Long artistId);

    // 활성 상태(isDeleted=false)인 다른 아티스트가 동일 이름을 사용하는지 확인
    boolean existsByArtistNameAndIsDeletedFalseAndArtistIdNot(String newName, Long artistId);
}
