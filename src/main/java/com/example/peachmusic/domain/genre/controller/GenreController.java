package com.example.peachmusic.domain.genre.controller;

import com.example.peachmusic.domain.genre.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GenreController {

    private final GenreService genreService;
}
