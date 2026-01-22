package com.example.peachmusic.domain.album.repository;

import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.domain.album.dto.response.AlbumSearchResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AlbumCustomRepository {

    Page<AlbumSearchResponseDto> findAlbumPageByWord(String word, Pageable pageable, UserRole role);
    List<AlbumSearchResponseDto> findAlbumListByWord(String word, int limit);
}
