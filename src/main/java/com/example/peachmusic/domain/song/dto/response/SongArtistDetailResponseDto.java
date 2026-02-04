package com.example.peachmusic.domain.song.dto.response;

import com.example.peachmusic.common.enums.JobStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class SongArtistDetailResponseDto {

    private final Long songId;
    private final String name;
    private final String artistName;
    private final Long likeCount;
    private final String albumImage;
    private final JobStatus jobStatus;
    private final boolean isLiked;
    private final Long albumId;
    private final LocalDate albumReleaseDate;
}
