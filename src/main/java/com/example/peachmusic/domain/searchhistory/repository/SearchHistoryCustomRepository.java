package com.example.peachmusic.domain.searchhistory.repository;

import com.example.peachmusic.domain.searchhistory.dto.SearchPopularResponseDto;
import java.util.List;

public interface SearchHistoryCustomRepository {
    List<SearchPopularResponseDto> findPopularKeyword();
}
