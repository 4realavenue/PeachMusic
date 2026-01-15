package com.example.peachmusic.domain.playlistSong.controller;

import com.example.peachmusic.domain.playlistSong.service.PlaylistSongService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PlaylistSongController {

    private final PlaylistSongService playlistSongService;
}
