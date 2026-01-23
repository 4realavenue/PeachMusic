package com.example.peachmusic.domain.artistAlbum.repository;

import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artistAlbum.entity.ArtistAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArtistAlbumRepository extends JpaRepository<ArtistAlbum, Long> {

    // 관리자용 - 앨범에 참여한 아티스트 조회
    List<ArtistAlbum> findAllByAlbum_AlbumId(Long albumId);
    // 유저용
    List<ArtistAlbum> findAllByAlbum_AlbumIdAndArtist_IsDeletedFalse(Long albumId);

    /**
     * 앨범 정책에 따라 참여 아티스트 목록을 전체 갱신하기 위해
     * 기존 ArtistAlbum 매핑을 하드 딜리트함
     * 동일 (artist_id, album_id) UNIQUE 제약 충돌을 방지하기 위해
     * 삭제 쿼리는 즉시 DB에 반영(flush)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ArtistAlbum aa where aa.album.albumId = :albumId")
    void deleteAllByAlbumId(@Param("albumId") Long albumId);

    /**
     * ArtistAlbum 매핑 테이블을 기준으로 앨범을 조회
     * N:M 구조로 인한 중복 앨범 제거를 위해 distinct 사용
     * 아티스트 비활성화/복구 로직에서 사용
     */
    @Query("""
            select distinct aa.album
            from ArtistAlbum aa
            where aa.artist.artistId = :artistId
            and aa.album.isDeleted = :isDeleted
            """)
    List<Album> findAlbumsByArtistIdAndIsDeleted(@Param("artistId") Long artistId, @Param("isDeleted") boolean isDeleted);

    boolean existsByArtist_ArtistIdAndAlbum_AlbumId(Long artistId, Long albumId);

    @Query("""
            select aa.artist from ArtistAlbum aa
            where aa.album.albumId = :albumId
            """)
    List<Artist> findArtist_ArtistIdByArtistAlbum_Album_AlbumId(Long albumId);

}
