package com.example.peachmusic.domain.playlistSong.service;

import com.example.peachmusic.domain.playlistSong.repository.PlaylistSongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaylistSongService {

    private final PlaylistSongRepository playlistSongRepository;
}
