package com.example.peachmusic.domain.artistlike.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class ArtistLikeCheckResponseDto {
    private final Set<Long> likedArtistIdSet;

    public static ArtistLikeCheckResponseDto from(Set<Long> likedArtistIdSet) {
        return new ArtistLikeCheckResponseDto(likedArtistIdSet);
    }
}
