package com.example.peachmusic.domain.song.model.response;

import com.example.peachmusic.domain.song.model.SongDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class SongCreateResponseDto {

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

    public static SongCreateResponseDto from(SongDto songDto, List<String> genre, Long albumId) {
        return new SongCreateResponseDto(songDto.getSongId(), songDto.getName(), songDto.getDuration(), songDto.getLicenseCcurl(), songDto.getPosition(), songDto.getVocalinstrumental(), songDto.getLang(), songDto.getSpeed(), genre, songDto.getInstruments(), songDto.getVartags(), albumId, songDto.getLikeCount());
    }

}
