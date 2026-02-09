package com.example.peachmusic.domain.songlike.service;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.Cursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.retry.LockRetryExecutor;
import com.example.peachmusic.common.service.AbstractKeysetService;
import com.example.peachmusic.domain.songlike.dto.response.SongLikeResponseDto;
import com.example.peachmusic.domain.songlike.dto.response.SongLikedItemResponseDto;
import com.example.peachmusic.domain.songlike.repository.SongLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SongLikeService extends AbstractKeysetService {

    private final LockRetryExecutor lockRetryExecutor;
    private final SongLikeCommand songLikeCommand;
    private final SongLikeRepository songLikeRepository;

    private static final int KEYSET_SIZE = 10;

    public SongLikeResponseDto likeSong(AuthUser authUser, Long songId) {
        return lockRetryExecutor.execute(() -> songLikeCommand.doLikeSong(authUser, songId));
    }

    @Transactional(readOnly = true)
    public KeysetResponse<SongLikedItemResponseDto> getMyLikedSong(Long userId, Long lastLikeId) {

        List<SongLikedItemResponseDto> content = songLikeRepository.findMyLikedSongWithCursor(userId, lastLikeId, KEYSET_SIZE);

        return toKeysetResponse(content, KEYSET_SIZE, likedSong -> new Cursor(likedSong.getSongLikeId(), null));
    }
}
