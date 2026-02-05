package com.example.peachmusic.domain.songlike.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SongLikedItemResponseDto {

    @JsonIgnore
    private final Long songLikeId;

    private final Long songId;
    private final String name;
    private final String audio;
    private final Long likeCount;
}
