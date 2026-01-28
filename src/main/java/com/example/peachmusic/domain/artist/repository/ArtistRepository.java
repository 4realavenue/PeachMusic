package com.example.peachmusic.domain.artist.repository;

import com.example.peachmusic.domain.artist.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArtistRepository extends JpaRepository<Artist, Long>, ArtistCustomRepository {

    Optional<Artist> findByArtistIdAndIsDeleted(Long artistId, boolean isDeleted);

    // 전달받은 artistIdList에 해당하는 활성 상태(isDeleted=false) 아티스트 조회
    List<Artist> findAllByArtistIdInAndIsDeletedFalse(List<Long> artistIds);

    @Modifying
    @Query("update Artist a set a.likeCount = a.likeCount + 1 where a.artistId = :artistId")
    void incrementLikeCount(@Param("artistId") Long artistId);

    @Modifying
    @Query("update Artist a set a.likeCount = a.likeCount - 1 where a.artistId = :artistId and a.likeCount > 0")
    void decrementLikeCount(@Param("artistId") Long artistId);
}
