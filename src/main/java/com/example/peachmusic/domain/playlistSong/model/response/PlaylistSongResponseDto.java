package com.example.peachmusic.domain.playlistSong.model.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PlaylistSongResponseDto {

        private final Long playlistSongId;
        private final Long songId;
        private final String name;
        private final Long duration;
        private final Long likeCount;
    }


