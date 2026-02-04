package com.example.peachmusic.domain.artist.dto.response;

import com.example.peachmusic.domain.album.dto.response.AlbumArtistDetailResponseDto;
import com.example.peachmusic.domain.album.dto.response.AlbumSearchResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongArtistDetailResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ArtistPreviewResponseDto {

    private final List<AlbumArtistDetailResponseDto> albumList;
    private final List<SongArtistDetailResponseDto> songList;

    public static ArtistPreviewResponseDto of(List<AlbumArtistDetailResponseDto> albumList, List<SongArtistDetailResponseDto> songList) {
        return new ArtistPreviewResponseDto(albumList, songList);
    }



}
