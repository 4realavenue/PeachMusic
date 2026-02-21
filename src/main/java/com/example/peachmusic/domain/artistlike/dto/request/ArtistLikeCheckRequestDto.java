package com.example.peachmusic.domain.artistlike.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class ArtistLikeCheckRequestDto {
    private List<Long> artistIdList;
}
