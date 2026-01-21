package com.example.peachmusic.domain.album.repository;

import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.domain.album.model.response.AlbumSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AlbumCustomRepository {

    Page<AlbumSearchResponse> findAlbumPageByWord(String word, Pageable pageable, UserRole role);
    List<AlbumSearchResponse> findAlbumListByWord(String word, int limit);
}
