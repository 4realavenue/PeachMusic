package com.example.peachmusic.domain.songGenre.controller;

import com.example.peachmusic.domain.songGenre.service.SongGenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SongGenreController {

    private final SongGenreService songGenreService;
}
