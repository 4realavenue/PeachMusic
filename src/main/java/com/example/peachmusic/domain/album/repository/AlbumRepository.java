package com.example.peachmusic.domain.album.repository;

import com.example.peachmusic.domain.album.entity.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    // 활성 상태(isDeleted=false)로 동일한 앨범이 존재하는지 여부 확인
    boolean existsByAlbumNameAndAlbumReleaseDateAndIsDeletedFalse(String albumName, LocalDate albumReleaseDate);

    // 비활성 상태(isDeleted=true)인 동일한 앨범 조회
    Optional<Album> findByAlbumNameAndAlbumReleaseDateAndIsDeletedTrue(String albumName, LocalDate albumReleaseDate);

    // 활성 상태(isDeleted=false)인 앨범 목록을 페이징 조건에 맞게 조회
    Page<Album> findAllByIsDeletedFalse(Pageable pageable);
}
