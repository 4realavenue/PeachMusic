package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.domain.album.dto.response.SongSummaryDto;
import com.example.peachmusic.domain.song.dto.response.SongArtistDetailResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface SongCustomRepository {

    List<SongSearchResponseDto> findSongKeysetPageByWord(String[] words, int size, boolean isAdmin, SortType sortType, SortDirection direction, Long lastId, Long lastLike, String lastName);
    List<SongSearchResponseDto> findSongListByWord(String[] words, int size, boolean isAdmin, SortType sortType, SortDirection direction);
    List<SongArtistDetailResponseDto> findSongList(Long userId, Long artistId, int size);
    List<SongArtistDetailResponseDto> findSongByArtistKeyset(Long userId, Long artistId, SortType sortType, SortDirection sortDirection, Long lastId, LocalDate lastDate, int size);

    List<SongSummaryDto> findSongSummaryListByAlbumId(Long albumId);
}

