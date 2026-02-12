package com.example.peachmusic.domain.song.dto.response;

import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.song.entity.Song;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SongPlayResponseDto {

    private final Long songId;
    private final String name;
    private final String albumName;
    private final String artistName;
    private final String streamingUrl;

    public static SongPlayResponseDto from(Song song, String artistName, String streamingUrl) {
        return new SongPlayResponseDto(song.getSongId(), song.getName(), song.getAlbum().getAlbumName(), artistName, streamingUrl);
    }

}
