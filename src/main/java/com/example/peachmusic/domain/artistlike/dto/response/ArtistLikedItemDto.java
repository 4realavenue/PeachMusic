package com.example.peachmusic.domain.artistlike.dto.response;

import com.example.peachmusic.domain.artistlike.repository.row.ArtistLikeRow;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ArtistLikedItemDto {

    private final Long artistId;
    private final String artistName;
    private final String profileImage;
    private final Long likeCount;

    public static ArtistLikedItemDto from(ArtistLikeRow row) {
        return new ArtistLikedItemDto(row.artistId(), row.artistName(), row.profileImage(), row.likeCount());
    }
}
