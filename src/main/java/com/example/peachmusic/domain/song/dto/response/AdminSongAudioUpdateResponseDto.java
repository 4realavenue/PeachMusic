package com.example.peachmusic.domain.song.dto.response;

import com.example.peachmusic.domain.song.entity.Song;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AdminSongAudioUpdateResponseDto {

    private final Long albumId;
    private final String albumName;
    private final Long position;

    private final Long songId;
    private final String name;
    private final String audio;


    public static AdminSongAudioUpdateResponseDto from(Song song) {
        return new AdminSongAudioUpdateResponseDto(song.getAlbum().getAlbumId(), song.getAlbum().getAlbumName(), song.getPosition(), song.getSongId(), song.getName(), song.getAudio());
    }
}
