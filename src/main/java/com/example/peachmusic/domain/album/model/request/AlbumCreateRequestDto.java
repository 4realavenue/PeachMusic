package com.example.peachmusic.domain.album.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class AlbumCreateRequestDto {

    @NotBlank(message = "앨범 이름은 필수입니다.")
    private String albumName;

    @NotNull(message = "앨범 발매일은 필수입니다.")
    private LocalDate albumReleaseDate;

    private String albumImage;

    @NotNull(message = "참여 아티스트는 필수입니다.")
    @Size(min = 1, message = "참여 아티스트는 최소 1명 이상이어야 합니다.")
    private List<@NotNull Long> artistIds;
}
