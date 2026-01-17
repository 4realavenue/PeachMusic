package com.example.peachmusic.domain.song.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AdminSongCreateRequestDto {

    private String name;
    private Long duration;
    private String licenseCcurl;
    private Long position;
    private String audio;
    private String vocalinstrumental;
    private String lang;
    private String speed;
    private List<Long> genreId;
    private String instruments;
    private String vartags;
    private Long albumId;

}
