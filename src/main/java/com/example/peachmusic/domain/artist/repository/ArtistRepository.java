package com.example.peachmusic.domain.artist.repository;

import com.example.peachmusic.domain.artist.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArtistRepository extends JpaRepository<Artist, Long>, ArtistCustomRepository {

    Optional<Artist> findByArtistIdAndIsDeletedFalse(Long artistId);

    // 활성 상태(isDeleted=false)인 다른 아티스트가 동일 이름을 사용하는지 확인
    boolean existsByArtistNameAndIsDeletedFalseAndArtistIdNot(String newName, Long artistId);

    Optional<Artist> findByArtistIdAndIsDeletedTrue(Long artistId);

    // 아티스트 이름으로 단건 조회 (삭제 여부와 관계없이 조회)
    Optional<Artist> findByArtistName(String artistName);

    // 전달받은 artistIds에 해당하는 활성 상태(isDeleted=false) 아티스트 조회
    List<Artist> findAllByArtistIdInAndIsDeletedFalse(List<Long> artistIds);
}
