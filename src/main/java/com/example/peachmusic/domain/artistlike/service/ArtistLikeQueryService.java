package com.example.peachmusic.domain.artistlike.service;

import com.example.peachmusic.common.model.Cursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.service.AbstractKeysetService;
import com.example.peachmusic.domain.artistlike.dto.response.ArtistLikedItemDto;
import com.example.peachmusic.domain.artistlike.repository.ArtistLikeRepository;
import com.example.peachmusic.domain.artistlike.repository.row.ArtistLikeRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistLikeQueryService extends AbstractKeysetService {

    private final ArtistLikeRepository artistLikeRepository;

    @Transactional(readOnly = true)
    public KeysetResponse<ArtistLikedItemDto> getMyLikedArtist(Long userId, Long lastId, Integer size) {

        List<ArtistLikeRow> rowList = artistLikeRepository.findMyLikedArtistWithCursor(userId, lastId, size);

        KeysetResponse<ArtistLikeRow> rowKeysetResponse = toKeysetResponse(rowList, size, row -> new Cursor(row.artistLikeId(), null));

        List<ArtistLikedItemDto> content = rowKeysetResponse.getContent().stream().map(ArtistLikedItemDto::from).toList();

        return new KeysetResponse<>(content, rowKeysetResponse.isHasNext(), rowKeysetResponse.getCursor());
    }
}
