package com.example.peachmusic.domain.songlike.service;

import com.example.peachmusic.common.model.Cursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.service.AbstractKeysetService;
import com.example.peachmusic.domain.songlike.dto.response.SongLikedItemResponseDto;
import com.example.peachmusic.domain.songlike.repository.SongLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SongLikeQueryService extends AbstractKeysetService {

    private final SongLikeRepository songLikeRepository;

    @Transactional(readOnly = true)
    public KeysetResponse<SongLikedItemResponseDto> getMyLikedSong(Long userId, Long lastLikeId) {

        final int size = 10;

        List<SongLikedItemResponseDto> content = songLikeRepository.findMyLikedSongWithCursor(userId, lastLikeId, size);

        return toKeysetResponse(content, size, likedSong -> new Cursor(likedSong.getSongLikeId(), null));
    }
}
