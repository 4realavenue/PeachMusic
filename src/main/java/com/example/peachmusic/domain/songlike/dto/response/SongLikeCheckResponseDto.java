package com.example.peachmusic.domain.songlike.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class SongLikeCheckResponseDto {
    private final Set<Long> likedSongIdSet;

    public static SongLikeCheckResponseDto from(Set<Long> likedSongIdSet) {
        return new SongLikeCheckResponseDto(likedSongIdSet);
    }
}
