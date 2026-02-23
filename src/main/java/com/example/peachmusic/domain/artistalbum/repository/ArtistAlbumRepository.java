package com.example.peachmusic.domain.artistalbum.repository;

import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artistalbum.entity.ArtistAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArtistAlbumRepository extends JpaRepository<ArtistAlbum, Long>, ArtistAlbumCustomRepository {

    // 관리자용 - 앨범에 참여한 아티스트 조회
    List<ArtistAlbum> findAllByAlbum_AlbumId(Long albumId);

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
     * 해당 아티스트가 참여한 앨범 ID 목록 (앨범 삭제 여부와 무관하게 전부)
     * delete/restore 모두 공통으로 사용
     */
    @Query("""
        select distinct aa.album.albumId
        from ArtistAlbum aa
        where aa.artist.artistId = :artistId
    """)
    List<Long> findDistinctAlbumIdListByArtistId(@Param("artistId") Long artistId);

    @Query("""
            select aa.artist from ArtistAlbum aa
            where aa.album.albumId = :albumId
            """)
    List<Artist> findArtist_ArtistIdByArtistAlbum_Album_AlbumId(Long albumId);
}
