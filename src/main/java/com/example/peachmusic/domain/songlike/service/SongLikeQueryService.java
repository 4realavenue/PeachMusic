package com.example.peachmusic.domain.songlike.service;

import com.example.peachmusic.common.model.Cursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.domain.songlike.dto.response.SongLikedItemDto;
import com.example.peachmusic.common.service.AbstractKeysetService;
import com.example.peachmusic.domain.songlike.dto.response.SongLikedItemResponseDto;
import com.example.peachmusic.domain.songlike.repository.SongLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static com.example.peachmusic.common.constants.SearchViewSize.DETAIL_SIZE;

@Service
@RequiredArgsConstructor
public class SongLikeQueryService {

    private final SongLikeRepository songLikeRepository;

    @Transactional(readOnly = true)
    public KeysetResponse<SongLikedItemResponseDto> getMyLikedSong(Long userId, Long lastLikeId) {

        final int size = DETAIL_SIZE;

        List<SongLikedItemResponseDto> content = songLikeRepository.findMyLikedSongWithCursor(userId, lastLikeId, size);

        return KeysetResponse.of(content, size, likedSong -> new Cursor(likedSong.getSongLikeId(), null));
    }
}
