package com.example.peachmusic.domain.albumlike.service;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.Cursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.retry.LockRetryExecutor;
import com.example.peachmusic.common.service.AbstractKeysetService;
import com.example.peachmusic.domain.albumlike.dto.response.AlbumLikeResponseDto;
import com.example.peachmusic.domain.albumlike.dto.response.AlbumLikedItemResponseDto;
import com.example.peachmusic.domain.albumlike.repository.AlbumLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumLikeService extends AbstractKeysetService {

    private final LockRetryExecutor lockRetryExecutor;
    private final AlbumLikeCommand albumLikeCommand;
    private final AlbumLikeRepository albumLikeRepository;

    private static final int KEYSET_SIZE = 10;

    public AlbumLikeResponseDto likeAlbum(AuthUser authUser, Long albumId) {
        return lockRetryExecutor.execute(() -> albumLikeCommand.doLikeAlbum(authUser, albumId));
    }

    @Transactional(readOnly = true)
    public KeysetResponse<AlbumLikedItemResponseDto> getMyLikedAlbum(Long userId, Long lastLikeId) {

        List<AlbumLikedItemResponseDto> content = albumLikeRepository.findMyLikedAlbumWithCursor(userId, lastLikeId, KEYSET_SIZE);

        // Cursor는 albumLikeId(lastId) 기준으로만 구성
        // 단일 정렬 기준이므로 lastSortValue는 사용하지 않음
        return toKeysetResponse(content, KEYSET_SIZE, likedAlbum -> new Cursor(likedAlbum.getAlbumLikeId(), null));
    }

    @Transactional(readOnly = true)
    public boolean isAlbumLiked(Long albumId, Long userId) {
        return albumLikeRepository.existsByAlbum_AlbumIdAndUser_UserId(albumId, userId);
    }
}