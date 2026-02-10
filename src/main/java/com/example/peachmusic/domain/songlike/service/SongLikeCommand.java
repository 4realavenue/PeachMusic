package com.example.peachmusic.domain.songlike.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.songlike.dto.response.SongLikeResponseDto;
import com.example.peachmusic.domain.songlike.repository.SongLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SongLikeCommand {

    private final SongLikeRepository songLikeRepository;
    private final SongRepository songRepository;

    /**
     * 음원 좋아요/좋아요 취소 기능
     */
    @Transactional
    public SongLikeResponseDto doLikeSong(AuthUser authUser, Long songId) {

        Long userId = authUser.getUserId();

        Song findSong = songRepository.findBySongIdAndIsDeletedFalse(songId)
                .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        boolean liked;

        int deleted = songLikeRepository.deleteBySongIdAndUserId(songId, userId);

        if (deleted == 1) {
            songRepository.decrementLikeCount(songId);
            liked = false;
        } else {
            int inserted = songLikeRepository.insertIgnore(userId, songId);

            if (inserted == 1) {
                songRepository.incrementLikeCount(songId);
            }
            liked = true;
        }
        Long likeCount = getSongLikeCount(songId);
        return SongLikeResponseDto.of(songId, findSong.getName(), liked, likeCount);
    }

    private Long getSongLikeCount(Long songId) {
        Long likeCount = songRepository.findLikeCountBySongId(songId);

        if (likeCount == null) {
            throw new CustomException(ErrorCode.SONG_NOT_FOUND);
        }
        return likeCount;
    }
}