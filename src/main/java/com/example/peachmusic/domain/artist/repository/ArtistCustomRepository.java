package com.example.peachmusic.domain.artist.repository;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CursorParam;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import java.util.List;

public interface ArtistCustomRepository {

    List<ArtistSearchResponseDto> findArtistKeysetPageByWord(AuthUser authUser, String word, int size, boolean isAdmin, SortType sortType, SortDirection direction, CursorParam cursor);
    List<ArtistSearchResponseDto> findArtistListByWord(String word, int size, boolean isAdmin, SortType sortType, SortDirection direction);
}
