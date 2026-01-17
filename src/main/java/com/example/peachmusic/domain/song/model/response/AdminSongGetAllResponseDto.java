package com.example.peachmusic.domain.song.model.response;

import com.example.peachmusic.domain.song.model.SongDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AdminSongGetAllResponseDto {

    private final Long songId;
    private final String name;
    private final Long duration;
    private final Long position;
    private final Long albumId;
    private final Long likeCount;

    public static AdminSongGetAllResponseDto from(SongDto songDto) {
        return new AdminSongGetAllResponseDto(songDto.getSongId(), songDto.getName(), songDto.getDuration(), songDto.getPosition(), songDto.getAlbumId(), songDto.getLikeCount());
    }

}
