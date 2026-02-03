package com.example.peachmusic.domain.artistlike.service;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.artistlike.dto.response.ArtistLikeResponseDto;
import lombok.RequiredArgsConstructor;
import org.hibernate.AssertionFailure;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class ArtistLikeService {

    private final ArtistLikeTxService artistLikeTxService;

    public ArtistLikeResponseDto likeArtist(AuthUser authUser, Long artistId) {
        return retryOnLock(() -> artistLikeTxService.doLikeArtist(authUser, artistId));
    }

    private ArtistLikeResponseDto retryOnLock(Supplier<ArtistLikeResponseDto> action) {
        int maxRetry = 2;

        for (int i = 0; i <= maxRetry; i++) {
            try {
                return action.get();
            } catch (PessimisticLockingFailureException e) {
                if (i == maxRetry) {
                    throw e;
                }
            }
        }
        throw new AssertionFailure("unreachable");
    }
}
