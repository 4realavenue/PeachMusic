package com.example.peachmusic.domain.artistLike.controller;

import com.example.peachmusic.domain.artistLike.service.ArtistLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ArtistLikeController {

    private final ArtistLikeService artistLikeService;
}
