package com.example.peachmusic.domain.songGenre.service;

import com.example.peachmusic.domain.songGenre.repository.SongGenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SongGenreService {

    private final SongGenreRepository songGenreRepository;
}
