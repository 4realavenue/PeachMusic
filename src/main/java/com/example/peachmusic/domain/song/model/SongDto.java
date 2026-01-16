package com.example.peachmusic.domain.song.model;

import com.example.peachmusic.domain.song.entity.Song;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class SongDto {

    private final Long songId;
    private final Long albumId;
    private final String name;
    private final Long duration;
    private final String licenseCcurl;
    private final Long position;
    private final String audio;
    private final String vocalinstrumental;
    private final String lang;
    private final String speed;
    private final String instruments;
    private final String vartags;
    private final Long likeCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public static SongDto from(Song song) {
        return new SongDto(song.getSongId(), song.getAlbum().getAlbumId(), song.getName(), song.getDuration(), song.getLicenseCcurl(), song.getPosition(), song.getAudio(), song.getVocalinstrumental(), song.getLang(), song.getSpeed(), song.getInstruments(), song.getVartags(), song.getLikeCount(), song.getCreatedAt(), song.getModifiedAt());
    }



}
