package com.example.peachmusic.domain.playlist.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlaylistCreateRequestDto {

    @NotBlank(message = "플레이리스트 이름 입력은 필수입니다.")
    private String playlistName;
}
