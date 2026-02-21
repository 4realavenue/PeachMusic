package com.example.peachmusic.domain.albumlike.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class AlbumLikeCheckResponseDto {
    private final Set<Long> likedAlbumIdSet;

    public static AlbumLikeCheckResponseDto from(Set<Long> likedAlbumIdSet) {
        return new AlbumLikeCheckResponseDto(likedAlbumIdSet);
    }
}
