package com.example.peachmusic.domain.searchHistory.repository;

import com.example.peachmusic.domain.searchHistory.dto.SearchPopularResponseDto;
import java.util.List;

public interface SearchHistoryCustomRepository {
    List<SearchPopularResponseDto> findPopularKeyword();
}
