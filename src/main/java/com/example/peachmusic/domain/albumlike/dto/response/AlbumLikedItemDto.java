package com.example.peachmusic.domain.albumlike.dto.response;

import com.example.peachmusic.domain.albumlike.repository.row.AlbumLikeRow;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AlbumLikedItemDto {

    private final Long albumId;
    private final String albumName;
    private final String albumImage;
    private final Long likeCount;

    public static AlbumLikedItemDto from(AlbumLikeRow row) {
        return new AlbumLikedItemDto(row.albumId(), row.albumName(), row.albumImage(), row.likeCount());
    }
}
