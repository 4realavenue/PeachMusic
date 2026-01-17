package com.example.peachmusic.domain.song.model.response;

import com.example.peachmusic.domain.song.model.SongDto;
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

    public static SongGetDetailResponseDto from(SongDto songDto, List<String> genres) {
        return new SongGetDetailResponseDto(songDto.getSongId(), songDto.getName(), songDto.getDuration(), songDto.getLicenseCcurl(), songDto.getPosition(), songDto.getAudio(), songDto.getVocalinstrumental(), songDto.getLang(), songDto.getSpeed(), genres, songDto.getInstruments(), songDto.getVartags(), songDto.getAlbumId(), songDto.getLikeCount());
    }
}
