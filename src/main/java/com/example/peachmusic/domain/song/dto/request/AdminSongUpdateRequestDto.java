package com.example.peachmusic.domain.song.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Getter
@NoArgsConstructor
public class AdminSongUpdateRequestDto {

    private Long albumId;

    private Long position;

    private String name;

    private Long duration;

    private String licenseCcurl;

    @URL(message = "URL 형식으로 맞춰서 입력해 주세요.")
    private String audio;

    private String vocalinstrumental;

    private String lang;

    private String speed;

    private List<Long> genreId;

    private String instruments;

    private String vartags;

}
