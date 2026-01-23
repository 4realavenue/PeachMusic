package com.example.peachmusic.domain.songLike.dto.response;

import com.example.peachmusic.domain.song.entity.Song;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SongLikeResponseDto {

    private final Long songId;
    private final String songName;
    private final boolean liked;
    private final Long likeCount;

    public static SongLikeResponseDto from(Song song, boolean isLiked) {
        return new SongLikeResponseDto(song.getSongId(), song.getName(), isLiked, song.getLikeCount());
    }
}
