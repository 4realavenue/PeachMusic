package com.example.peachmusic.domain.albumlike.service;

import com.example.peachmusic.common.model.Cursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.service.AbstractKeysetService;
import com.example.peachmusic.domain.albumlike.dto.response.AlbumLikedItemResponseDto;
import com.example.peachmusic.domain.albumlike.repository.AlbumLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumLikeQueryService extends AbstractKeysetService {

    private final AlbumLikeRepository albumLikeRepository;

    @Transactional(readOnly = true)
    public KeysetResponse<AlbumLikedItemResponseDto> getMyLikedAlbum(Long userId, Long lastLikeId) {

        final int size = 10;

        List<AlbumLikedItemResponseDto> content = albumLikeRepository.findMyLikedAlbumWithCursor(userId, lastLikeId, size);

        // Cursor는 albumLikeId(lastId) 기준으로만 구성
        // 단일 정렬 기준이므로 lastSortValue는 사용하지 않음
        return toKeysetResponse(content, size, likedAlbum -> new Cursor(likedAlbum.getAlbumLikeId(), null));
    }
}
