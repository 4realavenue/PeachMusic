package com.example.peachmusic.domain.song.model.response;

import com.example.peachmusic.domain.song.entity.Song;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class SongGetDetailResponseDto {

    private final Long songId;
    private final String name;
    private final Long duration;
    private final String licenseCcurl;
    private final Long position;
    private final String audio;
    private final String vocalinstrumental;
    private final String lang;
    private final String speed;
    private final List<String> genres;
    private final String instruments;
    private final String vartags;
    private final Long albumId;
    private final Long likeCount;

    public static SongGetDetailResponseDto from(Song song, List<String> genres) {
        return new SongGetDetailResponseDto(song.getSongId(), song.getName(), song.getDuration(), song.getLicenseCcurl(), song.getPosition(), song.getAudio(), song.getVocalinstrumental(), song.getLang(), song.getSpeed(), genres, song.getInstruments(), song.getVartags(), song.getAlbum().getAlbumId(), song.getLikeCount());
    }
}
