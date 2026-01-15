package com.example.peachmusic.domain.artistSong.controller;

import com.example.peachmusic.domain.artistSong.service.ArtistSongService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ArtistSongController {

    private final ArtistSongService artistSongService;
}
