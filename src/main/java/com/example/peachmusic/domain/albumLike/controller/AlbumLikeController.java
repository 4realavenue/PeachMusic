package com.example.peachmusic.domain.albumLike.controller;

import com.example.peachmusic.domain.albumLike.service.AlbumLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AlbumLikeController {

    private final AlbumLikeService albumLikeService;
}
