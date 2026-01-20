package com.example.peachmusic.domain.search.repository;

import com.example.peachmusic.domain.search.model.SearchPopularResponse;
import java.util.List;

public interface SearchCustomRepository {
    List<SearchPopularResponse> findPopularKeyword();
}
