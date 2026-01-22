package com.example.peachmusic.domain.album.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class AlbumUpdateRequestDto {

    private String albumName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate albumReleaseDate;

    @URL(message = "URL 형식으로 맞춰서 입력해 주세요.")
    private String albumImage;
}
