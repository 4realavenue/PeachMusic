package com.example.peachmusic.domain.artist.repository;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.model.CursorParam;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import java.util.List;

public interface ArtistCustomRepository {

    List<ArtistSearchResponseDto> findArtistKeysetPageByWord(String[] words, int size, boolean isAdmin, SortType sortType, SortDirection direction, CursorParam cursor);
    List<ArtistSearchResponseDto> findArtistListByWord(String[] words, int size, boolean isAdmin, SortType sortType, SortDirection direction);
}
