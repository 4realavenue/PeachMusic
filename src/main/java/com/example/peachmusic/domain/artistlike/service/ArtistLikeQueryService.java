package com.example.peachmusic.domain.artistlike.service;

import com.example.peachmusic.common.model.Cursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.domain.artistlike.dto.response.ArtistLikedItemDto;
import com.example.peachmusic.domain.artistlike.repository.ArtistLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static com.example.peachmusic.common.constants.SearchViewSize.DETAIL_SIZE;

@Service
@RequiredArgsConstructor
public class ArtistLikeQueryService {

    private final ArtistLikeRepository artistLikeRepository;

    @Transactional(readOnly = true)
    public KeysetResponse<ArtistLikedItemDto> getMyLikedArtist(Long userId, Long lastLikeId) {

        final int size = DETAIL_SIZE;

        List<ArtistLikedItemDto> content = artistLikeRepository.findMyLikedArtistWithCursor(userId, lastLikeId, size);

        return KeysetResponse.of(content, size, likedArtist -> new Cursor(likedArtist.getArtistLikeId(), null));
    }
}
