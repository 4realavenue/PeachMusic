package com.example.peachmusic.domain.song.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Getter
@AllArgsConstructor
public class AdminSongCreateRequestDto {

    @NotBlank
    private String name;

    private Long duration;

    private String licenseCcurl;

    private Long position;

    @URL
    @NotBlank
    private String audio;

    private String vocalinstrumental;

    private String lang;

    private String speed;

    private List<Long> genreId;

    private String instruments;

    private String vartags;

    private Long albumId;

}
