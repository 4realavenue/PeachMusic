package com.example.peachmusic.domain.songlike.service;

import com.example.peachmusic.common.model.Cursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.service.AbstractKeysetService;
import com.example.peachmusic.domain.songlike.dto.response.SongLikedItemDto;
import com.example.peachmusic.domain.songlike.repository.SongLikeRepository;
import com.example.peachmusic.domain.songlike.repository.row.SongLikeRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SongLikeQueryService extends AbstractKeysetService {

    private final SongLikeRepository songLikeRepository;

    @Transactional(readOnly = true)
    public KeysetResponse<SongLikedItemDto> getMyLikedSong(Long userId, Long lastId, Integer size) {

        List<SongLikeRow> rowList = songLikeRepository.findMyLikedSongWithCursor(userId, lastId, size);

        KeysetResponse<SongLikeRow> rowKeysetResponse = toKeysetResponse(rowList, size, row -> new Cursor(row.songLikeId(), null));

        List<SongLikedItemDto> content = rowKeysetResponse.getContent().stream().map(SongLikedItemDto::from).toList();

        return new KeysetResponse<>(content, rowKeysetResponse.isHasNext(), rowKeysetResponse.getCursor());
    }
}
