package com.example.peachmusic.domain.artistSong.service;

import com.example.peachmusic.domain.artistSong.repository.ArtistSongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArtistSongService {

    private final ArtistSongRepository artistSongRepository;
}
