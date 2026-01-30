package com.example.peachmusic.domain.albumlike.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.albumlike.dto.response.AlbumLikeResponseDto;
import com.example.peachmusic.domain.albumlike.repository.AlbumLikeRepository;
import com.example.peachmusic.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.hibernate.AssertionFailure;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class AlbumLikeService {

    private final AlbumLikeRepository albumLikeRepository;
    private final AlbumRepository albumRepository;
    private final UserService userService;

    /**
     * 앨범 좋아요 토글 기능
     *
     * @param authUser 인증된 사용자 정보
     * @param albumId  좋아요 토글할 앨범 ID
     * @return 토글 처리 결과(최종 좋아요 상태 및 좋아요 수)
     */
    @Transactional
    public AlbumLikeResponseDto likeAlbum(AuthUser authUser, Long albumId) {
        return retryOnLock(() -> doLikeAlbum(authUser, albumId));
    }

    private AlbumLikeResponseDto doLikeAlbum(AuthUser authUser, Long albumId) {

        userService.findUser(authUser);
        Long userId = authUser.getUserId();

        Album foundAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        int deleted = albumLikeRepository.deleteByAlbumIdAndUserId(albumId, userId);

        if (deleted == 1) {
            albumRepository.decrementLikeCount(albumId);

            return buildResponse(albumId, foundAlbum.getAlbumName(), false);
        }

        int inserted = albumLikeRepository.insertIgnore(userId, albumId);

        if (inserted == 1) {
            albumRepository.incrementLikeCount(albumId);

            return buildResponse(albumId, foundAlbum.getAlbumName(), true);
        }

        return buildResponse(albumId, foundAlbum.getAlbumName(), true);
    }

    private AlbumLikeResponseDto buildResponse(Long albumId, String albumName, boolean liked) {
        Long likeCount = albumRepository.findLikeCountByAlbumId(albumId);

        if (likeCount == null) {
            throw new CustomException(ErrorCode.ALBUM_NOT_FOUND);
        }

        return AlbumLikeResponseDto.of(albumId, albumName, liked, likeCount);
    }

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
        throw new AssertionFailure("unreachable");
    }
}