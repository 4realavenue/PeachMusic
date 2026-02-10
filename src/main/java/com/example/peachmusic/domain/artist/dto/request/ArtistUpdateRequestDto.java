package com.example.peachmusic.domain.artist.dto.request;

import com.example.peachmusic.common.enums.ArtistType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ArtistUpdateRequestDto {

    private String artistName;

    private String country;

    private ArtistType artistType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate debutDate;

    @Size(max = 500)
    private String bio;

    @AssertTrue(message = "수정할 값이 없습니다.")
    public boolean hasAnyUpdateField() {
        return (artistName != null && !artistName.isBlank())
                || (country != null && !country.isBlank())
                || artistType != null
                || debutDate != null
                || (bio != null && ! bio.isBlank());
    }
}
