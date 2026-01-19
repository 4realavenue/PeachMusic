package com.example.peachmusic.domain.song.model.response;

import com.example.peachmusic.domain.song.entity.Song;
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

    public static AdminSongGetAllResponseDto from(Song song) {
        return new AdminSongGetAllResponseDto(song.getSongId(), song.getName(), song.getDuration(), song.getPosition(), song.getAlbum().getAlbumId(), song.getLikeCount());
    }

}
