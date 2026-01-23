package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SongCustomRepository {

    Page<SongSearchResponse> findSongPageByWord(String word, Pageable pageable, UserRole role);

    List<SongSearchResponse> findSongListByWord(String word, int limit);
}
