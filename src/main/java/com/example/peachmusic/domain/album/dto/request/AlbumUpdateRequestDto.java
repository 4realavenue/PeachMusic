package com.example.peachmusic.domain.album.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class AlbumUpdateRequestDto {

    private String albumName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate albumReleaseDate;

    @AssertTrue(message = "수정할 값이 없습니다.")
    public boolean hasAnyUpdateField() {
        return (albumName != null && !albumName.isBlank())
                || albumReleaseDate != null;
    }
}
