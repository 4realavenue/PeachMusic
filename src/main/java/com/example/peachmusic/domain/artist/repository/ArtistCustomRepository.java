package com.example.peachmusic.domain.artist.repository;

import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ArtistCustomRepository {

    Page<ArtistSearchResponseDto> findArtistPageByWord(String word, Pageable pageable, UserRole role);
    List<ArtistSearchResponseDto> findArtistListByWord(String word, int limit);
}
