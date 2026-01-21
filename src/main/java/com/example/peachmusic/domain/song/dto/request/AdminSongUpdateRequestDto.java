package com.example.peachmusic.domain.song.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Getter
@NoArgsConstructor
public class AdminSongUpdateRequestDto {

    private Long albumId;

    private Long position;

    @NotBlank(message = "음원의 제목은 필수 입력 사항 입니다.")
    private String name;

    private Long duration;

    private String licenseCcurl;

    @URL(message = "URL 형식으로 맞춰서 입력해 주세요.")
    @NotBlank(message = "음원의 오디오는 필수 입력 사항 입니다.")
    private String audio;

    private String vocalinstrumental;

    private String lang;

    private String speed;

    private List<Long> genreId;

    private String instruments;

    private String vartags;

}
