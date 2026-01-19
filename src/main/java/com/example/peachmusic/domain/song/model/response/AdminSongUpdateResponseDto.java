package com.example.peachmusic.domain.song.model.response;

import com.example.peachmusic.domain.song.entity.Song;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class AdminSongUpdateResponseDto {

    private final Long songId;
    private final String name;
    private final Long duration;
    private final String licenseCcurl;
    private final Long position;
    private final String vocalinstrumental;
    private final String lang;
    private final String speed;
    private final List<String> genre;
    private final String instrumentals;
    private final String vartags;
    private final Long albumId;
    private final Long likeCount;

    public static AdminSongUpdateResponseDto from(Song song, List<String> genre, Long albumId) {
        return new AdminSongUpdateResponseDto(song.getSongId(), song.getName(), song.getDuration(), song.getLicenseCcurl(), song.getPosition(), song.getVocalinstrumental(), song.getLang(), song.getSpeed(), genre, song.getInstruments(), song.getVartags(), albumId, song.getLikeCount());
    }
}
