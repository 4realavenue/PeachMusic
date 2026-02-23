package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CursorParam;
import com.example.peachmusic.domain.album.dto.response.SongSummaryDto;
import com.example.peachmusic.domain.song.dto.response.SongArtistDetailResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
import java.util.List;

public interface SongCustomRepository {

    List<SongSearchResponseDto> findSongKeysetPageByWord(AuthUser authUser, String word, int size, boolean isAdmin, SortType sortType, SortDirection direction, CursorParam cursor);
    List<SongSearchResponseDto> findSongListByWord(AuthUser authUser, String word, int size, boolean isAdmin, SortType sortType, SortDirection direction);
    List<SongArtistDetailResponseDto> findSongList(AuthUser authUser, Long artistId, int size);
    List<SongArtistDetailResponseDto> findSongByArtistKeyset(AuthUser authUser, Long artistId, SortType sortType, SortDirection sortDirection, CursorParam cursor, int size);

    List<SongSummaryDto> findSongSummaryListByAlbumId(Long albumId, Long userId);
}

