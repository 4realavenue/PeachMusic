package com.example.peachmusic.domain.songLike.model;

import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.songLike.entity.SongLike;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SongLikeDto {

    private final Song song;
    private final boolean liked;
    private final Long likeCount;

    public static SongLikeDto from(Song song, boolean liked, Long likeCount) {
        return new SongLikeDto(song, liked, likeCount);
    }
}
