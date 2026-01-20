package com.example.peachmusic.domain.artist.repository;

import com.example.peachmusic.domain.artist.model.response.ArtistSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ArtistCustomRepository {

    Page<ArtistSearchResponse> findArtistPageByWord(String word, Pageable pageable);
    List<ArtistSearchResponse> findArtistListByWord(String word, int limit);
}
