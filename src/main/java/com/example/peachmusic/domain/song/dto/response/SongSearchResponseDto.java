package com.example.peachmusic.domain.song.dto.response;

import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.model.NextCursor;
import com.example.peachmusic.common.enums.ProgressingStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class SongSearchResponseDto {

    private final Long songId;
    private final String name;
    private final String artistName;
    private final String albumName;
    private final LocalDate releaseDate;
    private final String albumImage;
    private final Long likeCount;
    private final Long playCount;
    private final boolean isDeleted;
    private final ProgressingStatus progressingStatus;

    public NextCursor toCursor(SortType sortType) {
        return switch (sortType) {
            case LIKE -> new NextCursor(songId, likeCount);
            case NAME -> new NextCursor(songId, name);
            case RELEASE_DATE -> new NextCursor(songId, releaseDate);
            case PLAY -> new NextCursor(songId, playCount);
        };
    }

}
