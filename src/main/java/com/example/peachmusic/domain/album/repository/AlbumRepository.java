package com.example.peachmusic.domain.album.repository;

import com.example.peachmusic.domain.album.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    // 앨범 이름과 앨범 발매일로 단건 조회 (삭제 여부와 관계없이 조회)
    Optional<Album> findByAlbumNameAndAlbumReleaseDate(String albumName, LocalDate albumReleaseDate);

    // 활성 상태(isDeleted=false)인 앨범 조회
    Optional<Album> findByAlbumIdAndIsDeletedFalse(Long albumId);

    // 활성 상태(isDeleted=false)인 앨범 중, 현재 앨범을 제외하고 동일한 앨범이 존재하는지 확인
    boolean existsByAlbumNameAndAlbumReleaseDateAndIsDeletedFalseAndAlbumIdNot(String albumName, LocalDate albumReleaseDate, Long albumId);

    // 비활성 상태(isDeleted=true)인 앨범 조회
    Optional<Album> findByAlbumIdAndIsDeletedTrue(Long albumId);

    // (활성) 아티스트가 발매한 앨범 목록 조회
    List<Album> findAllByArtist_ArtistIdAndIsDeletedFalse(Long artistId);

    // (비활성) 아티스트가 발매한 앨범 목록 조회
    List<Album> findAllByArtist_ArtistIdAndIsDeletedTrue(Long artistId);

}
