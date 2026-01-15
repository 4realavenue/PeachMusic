package com.example.peachmusic.domain.songLike.controller;

import com.example.peachmusic.domain.songLike.service.SongLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SongLikeController {
    
    private final SongLikeService songLikeService;
}
