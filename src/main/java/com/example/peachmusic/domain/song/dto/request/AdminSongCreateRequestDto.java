package com.example.peachmusic.domain.song.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AdminSongCreateRequestDto {

    private Long albumId;

    private Long position;

    @NotBlank(message = "음원의 제목은 필수 입력 사항 입니다.")
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
