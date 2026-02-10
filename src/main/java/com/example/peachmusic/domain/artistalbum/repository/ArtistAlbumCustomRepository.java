package com.example.peachmusic.domain.artistalbum.repository;

import com.example.peachmusic.domain.album.dto.response.ArtistSummaryDto;

import java.util.List;

public interface ArtistAlbumCustomRepository {

    List<ArtistSummaryDto> findArtistSummaryListByAlbumId(Long albumId);
}
