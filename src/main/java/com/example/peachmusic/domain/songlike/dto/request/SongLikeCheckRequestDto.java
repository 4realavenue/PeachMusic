package com.example.peachmusic.domain.songlike.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class SongLikeCheckRequestDto {
    private List<Long> songIdList;
}
