package com.example.peachmusic.domain.albumlike.service;

import com.example.peachmusic.common.model.Cursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.service.AbstractKeysetService;
import com.example.peachmusic.domain.albumlike.dto.response.AlbumLikedItemDto;
import com.example.peachmusic.domain.albumlike.repository.AlbumLikeRepository;
import com.example.peachmusic.domain.albumlike.repository.row.AlbumLikeRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumLikeQueryService extends AbstractKeysetService {

    private final AlbumLikeRepository albumLikeRepository;

    @Transactional(readOnly = true)
    public KeysetResponse<AlbumLikedItemDto> getMyLikedAlbum(Long userId, Long lastId, Integer size) {

        List<AlbumLikeRow> rowList = albumLikeRepository.findMyLikedAlbumWithCursor(userId, lastId, size);

        // Cursor는 albumLikeId(lastId) 기준으로만 구성
        // 단일 정렬 기준이므로 lastSortValue는 사용하지 않음
        KeysetResponse<AlbumLikeRow> rowKeysetResponse = toKeysetResponse(rowList, size, row -> new Cursor(row.albumLikeId(), null));

        List<AlbumLikedItemDto> content = rowKeysetResponse.getContent().stream().map(AlbumLikedItemDto::from).toList();

        return new KeysetResponse<>(content, rowKeysetResponse.isHasNext(), rowKeysetResponse.getCursor());
    }
}
