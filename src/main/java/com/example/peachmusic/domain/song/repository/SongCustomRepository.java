package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
import java.util.List;

public interface SongCustomRepository {

    List<SongSearchResponseDto> findSongKeysetPageByWord(String word, int size, boolean isAdmin, SortType sortType, SortDirection direction, Long lastId, Long lastLike, String lastName);
    List<SongSearchResponseDto> findSongListByWord(String word, int size, boolean isAdmin, SortType sortType, SortDirection direction);
}
