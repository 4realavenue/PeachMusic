package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.domain.song.model.response.SongSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface SongCustomRepository {

    Page<SongSearchResponse> findSongPageByWord(String word, Pageable pageable);
    List<SongSearchResponse> findSongListByWord(String word, int limit);
}
