package com.example.peachmusic.domain.artistlike.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ArtistLikedItemResponseDto {

    @JsonIgnore
    private final Long artistLikeId;

    private final Long artistId;
    private final String artistName;
    private final String profileImage;
    private final Long likeCount;
}
