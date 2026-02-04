package com.example.peachmusic.domain.artistlike.service;

import com.example.peachmusic.common.model.Cursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.service.AbstractKeysetService;
import com.example.peachmusic.domain.artistlike.dto.response.ArtistLikedItemDto;
import com.example.peachmusic.domain.artistlike.repository.ArtistLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistLikeQueryService extends AbstractKeysetService {

    private final ArtistLikeRepository artistLikeRepository;

    @Transactional(readOnly = true)
    public KeysetResponse<ArtistLikedItemDto> getMyLikedArtist(Long userId, Long lastLikeId) {

        final int size = 10;

        List<ArtistLikedItemDto> content = artistLikeRepository.findMyLikedArtistWithCursor(userId, lastLikeId, size);

        return toKeysetResponse(content, size, likedArtist -> new Cursor(likedArtist.getArtistLikeId(), null));
    }
}
