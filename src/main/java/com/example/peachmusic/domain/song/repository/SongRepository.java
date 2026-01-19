package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.domain.song.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    // 활성 상태(isDeleted=false)인 앨범 음원을 음원 순서 오름차순으로 조회
    List<Song> findAllByAlbum_AlbumIdAndIsDeletedFalseOrderByPositionAsc(Long albumId);
}
