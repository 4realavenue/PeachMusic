package com.example.peachmusic.domain.albumlike.service;

import com.example.peachmusic.common.model.Cursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.service.AbstractKeysetService;
import com.example.peachmusic.domain.albumlike.dto.response.AlbumLikedItemDto;
import com.example.peachmusic.domain.albumlike.repository.AlbumLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static com.example.peachmusic.common.constants.SearchViewSize.DETAIL_SIZE;

@Service
@RequiredArgsConstructor
public class AlbumLikeQueryService extends AbstractKeysetService {

    private final AlbumLikeRepository albumLikeRepository;

    @Transactional(readOnly = true)
    public KeysetResponse<AlbumLikedItemDto> getMyLikedAlbum(Long userId, Long lastLikeId) {

        final int size = DETAIL_SIZE;

        List<AlbumLikedItemDto> content = albumLikeRepository.findMyLikedAlbumWithCursor(userId, lastLikeId, size);

        // Cursor는 albumLikeId(lastId) 기준으로만 구성
        // 단일 정렬 기준이므로 lastSortValue는 사용하지 않음
        return KeysetResponse.of(content, size, likedAlbum -> new Cursor(likedAlbum.getAlbumLikeId(), null));
    }
}
