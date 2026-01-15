package com.example.peachmusic.domain.artistAlbum.service;

import com.example.peachmusic.domain.artistAlbum.repository.ArtistAlbumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArtistAlbumService {
    
    private final ArtistAlbumRepository artistAlbumRepository;
}
