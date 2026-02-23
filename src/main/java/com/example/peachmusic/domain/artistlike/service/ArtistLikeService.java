package com.example.peachmusic.domain.artistlike.service;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.model.NextCursor;
import com.example.peachmusic.common.retry.LockRetryExecutor;
import com.example.peachmusic.domain.artistlike.dto.response.ArtistLikeResponseDto;
import com.example.peachmusic.domain.artistlike.dto.response.ArtistLikedItemResponseDto;
import com.example.peachmusic.domain.artistlike.repository.ArtistLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.peachmusic.common.constants.SearchViewSize.DETAIL_SIZE;

@Service
@RequiredArgsConstructor
public class ArtistLikeService {

    private final LockRetryExecutor lockRetryExecutor;
    private final ArtistLikeCommand artistLikeCommand;
    private final ArtistLikeRepository artistLikeRepository;

    private static final int SIZE = DETAIL_SIZE;

    public ArtistLikeResponseDto likeArtist(AuthUser authUser, Long artistId) {
        return lockRetryExecutor.execute(() -> artistLikeCommand.doLikeArtist(authUser, artistId));
    }

    @Transactional(readOnly = true)
    public KeysetResponse<ArtistLikedItemResponseDto> getMyLikedArtist(Long userId, Long lastLikeId) {

        List<ArtistLikedItemResponseDto> content = artistLikeRepository.findMyLikedArtistWithCursor(userId, lastLikeId, SIZE);

        return KeysetResponse.of(content, SIZE, likedArtist -> new NextCursor(likedArtist.getArtistLikeId(), null));
    }

    @Transactional(readOnly = true)
    public boolean isArtistLiked(Long artistId, Long userId) {
        return artistLikeRepository.existsByArtist_ArtistIdAndUser_UserId(artistId, userId);
    }
}
