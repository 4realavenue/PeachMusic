package com.example.peachmusic.domain.song.dto.response;

import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.song.entity.Song;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class AdminSongUpdateResponseDto {

    private final Long albumId;
    private final String albumName;
    private final Long position;

    private final Long songId;
    private final String name;
    private final String audio;
    private final Long duration;
    private final String licenseCcurl;
    private final String vocalinstrumental;
    private final String lang;
    private final String speed;
    private final List<String> genre;
    private final String instrumentals;
    private final String vartags;

    public static AdminSongUpdateResponseDto from(Song song, List<String> genreNameList, Album album) {
        return new AdminSongUpdateResponseDto(album.getAlbumId(), album.getAlbumName(), song.getPosition(), song.getSongId(), song.getName(), song.getAudio(), song.getDuration(), song.getLicenseCcurl(), song.getVocalinstrumental(), song.getLang(), song.getSpeed(), genreNameList, song.getInstruments(), song.getVartags());
    }
}
