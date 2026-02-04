package com.example.peachmusic.domain.songlike.dto.response;

import com.example.peachmusic.domain.songlike.repository.row.SongLikeRow;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SongLikedItemDto {

    private final Long songId;
    private final String name;
    private final String audio;
    private final Long likeCount;

    public static SongLikedItemDto from(SongLikeRow row) {
        return new SongLikedItemDto(row.songId(), row.name(), row.audio(), row.likeCount());
    }
}
