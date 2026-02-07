package com.example.peachmusic.domain.song.dto.response;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.enums.JobStatus;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.Cursor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SongSearchResponseDto {

    private final Long songId;
    private final String name;
    private final String artistName;
    private final Long likeCount;
    private final String albumImage;
    private final boolean isDeleted;
    private final JobStatus jobStatus;

    public Cursor toCursor(SortType sortType) {
        return switch (sortType) {
            case LIKE -> new Cursor(songId, likeCount);
            case NAME -> new Cursor(songId, name);
            default -> throw new CustomException(ErrorCode.UNSUPPORTED_SORT_TYPE);
        };
    }
}
