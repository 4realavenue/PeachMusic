package com.example.peachmusic.domain.song.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SongPlayResponseDto {

    private final String streamingUrl;

    public static SongPlayResponseDto from(String streamingUrl) {
        return new SongPlayResponseDto(streamingUrl);
    }

}
