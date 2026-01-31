package com.example.peachmusic.domain.album.repository;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.domain.album.dto.response.AlbumSearchResponseDto;
import java.util.List;

public interface AlbumCustomRepository {

    List<AlbumSearchResponseDto> findAlbumKeysetPageByWord(String word, int size, boolean isAdmin, SortType sortType, SortDirection direction, Long lastId, Long lastLike, String lastName);
    List<AlbumSearchResponseDto> findAlbumListByWord(String word, int size, boolean isAdmin, SortType sortType, SortDirection direction);
}
