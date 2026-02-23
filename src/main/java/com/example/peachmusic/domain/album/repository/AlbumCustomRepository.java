package com.example.peachmusic.domain.album.repository;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CursorParam;
import com.example.peachmusic.domain.album.dto.response.AlbumArtistDetailResponseDto;
import com.example.peachmusic.domain.album.dto.response.AlbumSearchResponseDto;
import java.util.List;

public interface AlbumCustomRepository {

    List<AlbumSearchResponseDto> findAlbumKeysetPageByWord(AuthUser authUser, String word, int size, boolean isAdmin, SortType sortType, SortDirection direction, CursorParam cursor);
    List<AlbumSearchResponseDto> findAlbumListByWord(AuthUser authUser, String word, int size, boolean isAdmin, SortType sortType, SortDirection direction);
    List<AlbumArtistDetailResponseDto> findAlbumList(AuthUser authUser, Long artistId, int size);
    List<AlbumArtistDetailResponseDto> findAlbumByArtistKeyset(AuthUser authUser, Long artistId, SortType sortType, SortDirection sortDirection, CursorParam cursor, int size);
}
