package com.example.peachmusic.domain.artist.dto.response;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.Cursor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ArtistSearchResponseDto {

    private final Long artistId;
    private final String artistName;
    private final Long likeCount;
    private final boolean isDeleted;

    public Cursor toCursor(SortType sortType) {
        return switch (sortType) {
            case LIKE -> new Cursor(artistId, likeCount);
            case NAME -> new Cursor(artistId, artistName);
            default -> throw new CustomException(ErrorCode.UNSUPPORTED_SORT_TYPE);
        };
    }
}
