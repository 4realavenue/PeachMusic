package com.example.peachmusic.domain.song.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AdminSongUpdateRequestDto {

    private Long albumId;

    private Long position;

    private String name;

    private Long duration;

    private String licenseCcurl;

    private String vocalinstrumental;

    private String lang;

    private String speed;

    private List<Long> genreIdList;

    private String instruments;

    private String vartags;

}
