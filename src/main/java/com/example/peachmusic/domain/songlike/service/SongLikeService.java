package com.example.peachmusic.domain.songlike.service;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.songlike.dto.response.SongLikeResponseDto;
import lombok.RequiredArgsConstructor;
import org.hibernate.AssertionFailure;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class SongLikeService {

    private final SongLikeTxService songLikeTxService;

    public SongLikeResponseDto likeSong(AuthUser authUser, Long songId) {
        return retryOnLock(() -> songLikeTxService.doLikeSong(authUser, songId));
    }

    private SongLikeResponseDto retryOnLock(Supplier<SongLikeResponseDto> action) {
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
