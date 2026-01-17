package com.example.peachmusic.domain.song.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AdminSongCreateRequestDto {

    private final String name;
    private final Long duration;
    private final String licenseCcurl;
    private final Long position;
    private final String audio;
    private final String vocalinstrumental;
    private final String lang;
    private final String speed;
    private final List<Long> genreId;
    private final String instruments;
    private final String vartags;
    private final Long albumId;

}
