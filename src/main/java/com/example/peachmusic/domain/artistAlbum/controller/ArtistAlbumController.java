package com.example.peachmusic.domain.artistAlbum.controller;


import com.example.peachmusic.domain.artistAlbum.service.ArtistAlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ArtistAlbumController {

    private final ArtistAlbumService artistAlbumService;
}
