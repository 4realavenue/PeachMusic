package com.example.peachmusic.domain.albumlike.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AlbumLikedItemResponseDto {

    @JsonIgnore
    private final Long albumLikeId; // 커서용 (응답 제외)

    private final Long albumId;
    private final String albumName;
    private final String albumImage;
    private final Long likeCount;
}
