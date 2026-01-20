package com.example.peachmusic.domain.album.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ArtistAlbumUpdateRequestDto {

    @NotNull(message = "참여 아티스트는 필수입니다.")
    @Size(min = 1, message = "참여 아티스트는 최소 1명 이상이어야 합니다.")
    private List<Long> artistIds;
}
