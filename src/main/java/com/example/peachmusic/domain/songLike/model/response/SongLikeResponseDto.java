package com.example.peachmusic.domain.songLike.model.response;

import com.example.peachmusic.domain.songLike.model.SongLikeDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SongLikeResponseDto {

    private final Long songId;
    private final boolean liked;
    private final Long likeCount;

    public static SongLikeResponseDto from(SongLikeDto songLikeDto) {
        return new SongLikeResponseDto(songLikeDto.getSong().getSongId(), songLikeDto.isLiked(), songLikeDto.getLikeCount());
    }
}
