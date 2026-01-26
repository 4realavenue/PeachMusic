package com.example.peachmusic.domain.artist.repository;

import com.example.peachmusic.domain.artist.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArtistRepository extends JpaRepository<Artist, Long>, ArtistCustomRepository {

    Optional<Artist> findByArtistIdAndIsDeleted(Long artistId, boolean isDeleted);

    // 전달받은 artistIdList에 해당하는 활성 상태(isDeleted=false) 아티스트 조회
    List<Artist> findAllByArtistIdInAndIsDeletedFalse(List<Long> artistIds);
}
