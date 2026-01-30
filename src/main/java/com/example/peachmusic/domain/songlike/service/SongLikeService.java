package com.example.peachmusic.domain.songlike.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.songlike.dto.response.SongLikeResponseDto;
import com.example.peachmusic.domain.songlike.repository.SongLikeRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.AssertionFailure;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class SongLikeService {

    private final SongLikeRepository songLikeRepository;
    private final SongRepository songRepository;

    /**
     * 음원 좋아요/좋아요 취소 기능
     */
    @Transactional
    public SongLikeResponseDto likeSong(AuthUser authUser, Long songId) {
        return retryOnLock(() -> doLikeSong(authUser, songId));
    }

    private SongLikeResponseDto doLikeSong(AuthUser authUser, Long songId) {

        Long userId = authUser.getUserId();

        Song findSong = songRepository.findBySongIdAndIsDeletedFalse(songId)
                .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        int deleted = songLikeRepository.deleteBySongIdAndUserId(songId, userId);

        if (deleted == 1) {
            songRepository.decrementLikeCount(songId);

            return buildResponse(songId, findSong.getName(), false);
        }

        int inserted = songLikeRepository.insertIgnore(userId, songId);

        if (inserted == 1) {
            songRepository.incrementLikeCount(songId);

            return buildResponse(songId, findSong.getName(), true);
        }
        return buildResponse(songId, findSong.getName(), true);
    }

    private SongLikeResponseDto buildResponse(Long songId, String songName, boolean liked) {
        Long likeCount = songRepository.findLikeCountBySongId(songId);

        if (likeCount == null) {
            throw new CustomException(ErrorCode.SONG_NOT_FOUND);
        }
        return SongLikeResponseDto.of(songId, songName, liked, likeCount);
    }

    private SongLikeResponseDto retryOnLock(Supplier<SongLikeResponseDto> action) {
        int maxRetry = 2;

        for (int i = 0; i <= maxRetry; i++) {
            try {
                return action.get();
            } catch (PessimisticLockingFailureException e) {
                if (i == maxRetry) {
                    throw e;
                }
            }
        }
        throw new AssertionFailure("unreachable");
    }
}
