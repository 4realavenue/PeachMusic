package com.example.peachmusic.domain.albumlike.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class AlbumLikeCheckRequestDto {
    private List<Long> albumIdList;
}
