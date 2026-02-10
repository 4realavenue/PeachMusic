package com.example.peachmusic.domain.songlike.service;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.model.NextCursor;
import com.example.peachmusic.common.retry.LockRetryExecutor;
import com.example.peachmusic.domain.songlike.dto.response.SongLikeResponseDto;
import com.example.peachmusic.domain.songlike.dto.response.SongLikedItemResponseDto;
import com.example.peachmusic.domain.songlike.repository.SongLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.peachmusic.common.constants.SearchViewSize.DETAIL_SIZE;

@Service
@RequiredArgsConstructor
public class SongLikeService {

    private final LockRetryExecutor lockRetryExecutor;
    private final SongLikeCommand songLikeCommand;
    private final SongLikeRepository songLikeRepository;

    private static final int SIZE = DETAIL_SIZE;

    public SongLikeResponseDto likeSong(AuthUser authUser, Long songId) {
        return lockRetryExecutor.execute(() -> songLikeCommand.doLikeSong(authUser, songId));
    }

    @Transactional(readOnly = true)
    public KeysetResponse<SongLikedItemResponseDto> getMyLikedSong(Long userId, Long lastLikeId) {

        List<SongLikedItemResponseDto> content = songLikeRepository.findMyLikedSongWithCursor(userId, lastLikeId, SIZE);

        return KeysetResponse.of(content, SIZE, likedSong -> new NextCursor(likedSong.getSongLikeId(), null));
    }
}
