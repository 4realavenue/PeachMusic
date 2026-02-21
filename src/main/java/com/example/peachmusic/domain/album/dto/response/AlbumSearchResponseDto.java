package com.example.peachmusic.domain.album.dto.response;

import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.model.NextCursor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class AlbumSearchResponseDto {

    private final Long albumId;
    private final String albumName;
    private final String artistName;
    private final LocalDate releaseDate;
    private final String albumImage;
    private final Long likeCount;
    private final boolean isDeleted;

    public NextCursor toCursor(SortType sortType) {
        return switch (sortType) {
            case LIKE -> new NextCursor(albumId, likeCount);
            case NAME -> new NextCursor(albumId, albumName);
            case RELEASE_DATE -> new NextCursor(albumId, releaseDate);
            default -> null;
        };
    }
}
