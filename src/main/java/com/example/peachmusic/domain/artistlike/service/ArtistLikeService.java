package com.example.peachmusic.domain.artistlike.service;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.Cursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.retry.LockRetryExecutor;
import com.example.peachmusic.common.service.AbstractKeysetService;
import com.example.peachmusic.domain.artistlike.dto.response.ArtistLikeResponseDto;
import com.example.peachmusic.domain.artistlike.dto.response.ArtistLikedItemResponseDto;
import com.example.peachmusic.domain.artistlike.repository.ArtistLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistLikeService extends AbstractKeysetService {

    private final LockRetryExecutor lockRetryExecutor;
    private final ArtistLikeCommand artistLikeCommand;
    private final ArtistLikeRepository artistLikeRepository;

    private static final int KEYSET_SIZE = 10;

    public ArtistLikeResponseDto likeArtist(AuthUser authUser, Long artistId) {
        return lockRetryExecutor.execute(() -> artistLikeCommand.doLikeArtist(authUser, artistId));
    }

    @Transactional(readOnly = true)
    public KeysetResponse<ArtistLikedItemResponseDto> getMyLikedArtist(Long userId, Long lastLikeId) {

        List<ArtistLikedItemResponseDto> content = artistLikeRepository.findMyLikedArtistWithCursor(userId, lastLikeId, KEYSET_SIZE);

        return toKeysetResponse(content, KEYSET_SIZE, likedArtist -> new Cursor(likedArtist.getArtistLikeId(), null));
    }

    @Transactional(readOnly = true)
    public boolean isArtistLiked(Long artistId, Long userId) {
        return artistLikeRepository.existsByArtist_ArtistIdAndUser_UserId(artistId, userId);
    }
}
