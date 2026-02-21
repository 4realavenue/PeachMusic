package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.model.CursorParam;
import com.example.peachmusic.domain.album.dto.response.SongSummaryDto;
import com.example.peachmusic.domain.song.dto.response.SongArtistDetailResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
import java.util.List;

public interface SongCustomRepository {

    List<SongSearchResponseDto> findSongKeysetPageByWord(String word, int size, boolean isAdmin, SortType sortType, SortDirection direction, CursorParam cursor);
    List<SongSearchResponseDto> findSongListByWord(String word, int size, boolean isAdmin, SortType sortType, SortDirection direction);
    List<SongArtistDetailResponseDto> findSongList(Long artistId, int size);
    List<SongArtistDetailResponseDto> findSongByArtistKeyset(Long artistId, SortType sortType, SortDirection sortDirection, CursorParam cursor, int size);

    List<SongSummaryDto> findSongSummaryListByAlbumId(Long albumId, Long userId);
}

