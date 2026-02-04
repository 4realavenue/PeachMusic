package com.example.peachmusic.domain.album.repository;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.domain.album.dto.response.AlbumArtistDetailResponseDto;
import com.example.peachmusic.domain.album.dto.response.AlbumSearchResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface AlbumCustomRepository {

    List<AlbumSearchResponseDto> findAlbumKeysetPageByWord(String[] words, int size, boolean isAdmin, SortType sortType, SortDirection direction, Long lastId, Long lastLike, String lastName);
    List<AlbumSearchResponseDto> findAlbumListByWord(String[] words, int size, boolean isAdmin, SortType sortType, SortDirection direction);
    List<AlbumArtistDetailResponseDto> findAlbumList(Long userId, Long artistId, int size);
    List<AlbumArtistDetailResponseDto> findAlbumByArtistKeyset(Long userId, Long artistId, SortType sortType, SortDirection sortDirection, Long lastId, LocalDate lastDate, int size);
}
