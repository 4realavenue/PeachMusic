package com.example.peachmusic.domain.albumlike.service;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.model.NextCursor;
import com.example.peachmusic.common.retry.LockRetryExecutor;
import com.example.peachmusic.domain.albumlike.dto.request.AlbumLikeCheckRequestDto;
import com.example.peachmusic.domain.albumlike.dto.response.AlbumLikeCheckResponseDto;
import com.example.peachmusic.domain.albumlike.dto.response.AlbumLikeResponseDto;
import com.example.peachmusic.domain.albumlike.dto.response.AlbumLikedItemResponseDto;
import com.example.peachmusic.domain.albumlike.repository.AlbumLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.example.peachmusic.common.constants.SearchViewSize.DETAIL_SIZE;

@Service
@RequiredArgsConstructor
public class AlbumLikeService {

    private final LockRetryExecutor lockRetryExecutor;
    private final AlbumLikeCommand albumLikeCommand;
    private final AlbumLikeRepository albumLikeRepository;

    private static final int SIZE = DETAIL_SIZE;

    public AlbumLikeResponseDto likeAlbum(AuthUser authUser, Long albumId) {
        return lockRetryExecutor.execute(() -> albumLikeCommand.doLikeAlbum(authUser, albumId));
    }

    @Transactional(readOnly = true)
    public KeysetResponse<AlbumLikedItemResponseDto> getMyLikedAlbum(Long userId, Long lastLikeId) {

        List<AlbumLikedItemResponseDto> content = albumLikeRepository.findMyLikedAlbumWithCursor(userId, lastLikeId, SIZE);

        // Cursor는 albumLikeId(lastId) 기준으로만 구성
        // 단일 정렬 기준이므로 lastSortValue는 사용하지 않음
        return KeysetResponse.of(content, SIZE, likedAlbum -> new NextCursor(likedAlbum.getAlbumLikeId(), null));
    }

    @Transactional(readOnly = true)
    public boolean isAlbumLiked(Long albumId, Long userId) {
        return albumLikeRepository.existsByAlbum_AlbumIdAndUser_UserId(albumId, userId);
    }

    @Transactional(readOnly = true)
    public AlbumLikeCheckResponseDto checkAlbumLike(AuthUser authUser, AlbumLikeCheckRequestDto request) {

        if (request.getAlbumIdList() == null || request.getAlbumIdList().isEmpty()) {
            return AlbumLikeCheckResponseDto.from(Collections.emptySet());
        }

        Set<Long> likedAlbumIdSet = albumLikeRepository.findLikedAlbumIdList(authUser.getUserId(), request.getAlbumIdList());

        return AlbumLikeCheckResponseDto.from(likedAlbumIdSet);
    }
}