package com.example.peachmusic.domain.albumlike.service;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.albumlike.dto.response.AlbumLikeResponseDto;
import lombok.RequiredArgsConstructor;
import org.hibernate.AssertionFailure;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class AlbumLikeService {

    private final AlbumLikeTxService albumLikeTxService;

    /**
     * 재시도 역할만 담당하고, 실제 DB 작업은 TxService에서 처리
     */
    public AlbumLikeResponseDto likeAlbum(AuthUser authUser, Long albumId) {
        return retryOnLock(() -> albumLikeTxService.doLikeAlbum(authUser, albumId));
    }

    /**
     * 락/데드락 발생 시 재시도하는 공통 로직
     */
    private AlbumLikeResponseDto retryOnLock(Supplier<AlbumLikeResponseDto> action) {
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
        // 논리적으로 도달 불가: 성공 시 return, 실패 시 위에서 예외 throw
        throw new AssertionFailure("unreachable");
    }
}