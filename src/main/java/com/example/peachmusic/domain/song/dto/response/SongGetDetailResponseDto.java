package com.example.peachmusic.domain.song.dto.response;

import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.song.entity.Song;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class SongGetDetailResponseDto {

    private final Long albumId;
    private final String albumName;
    private final String albumImage;
    private final Long position;

    private final String artistName;

    private final Long songId;
    private final String name;
    private final String audio;
    private final Long duration;
    private final String licenseCcurl;
    private final String vocalinstrumental;
    private final String lang;
    private final String speed;
    private final List<String> genreList;
    private final String instrumentals;
    private final String vartags;

    private final Long likeCount;
    private final boolean liked;

    public static SongGetDetailResponseDto from(Album album, String artistName, Song song, List<String> genreNameList, boolean liked) {
        return new SongGetDetailResponseDto(album.getAlbumId(), album.getAlbumName(), album.getAlbumImage(), song.getPosition(), artistName, song.getSongId(), song.getName(), song.getAudio(), song.getDuration(), song.getLicenseCcurl(), song.getVocalinstrumental(), song.getLang(), song.getSpeed(), genreNameList, song.getInstruments(), song.getVartags(), song.getLikeCount(), liked);
    }
}
