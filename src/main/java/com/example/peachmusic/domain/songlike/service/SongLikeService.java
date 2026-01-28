package com.example.peachmusic.domain.songlike.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.songlike.dto.response.SongLikeResponseDto;
import com.example.peachmusic.domain.songlike.repository.SongLikeRepository;
import com.example.peachmusic.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SongLikeService {

    private final SongLikeRepository songLikeRepository;
    private final SongRepository songRepository;
    private final UserService userService;

    /**
     * 음원 좋아요/좋아요 취소 기능
     */
    @Transactional
    public SongLikeResponseDto likeSong(AuthUser authUser, Long songId) {

        userService.findUser(authUser); // 유저 유효성 검증
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

        // inserted == 0: 이미 좋아요가 존재함 (동시 요청으로 다른 요청이 먼저 생성했을 수 있음)
        return buildResponse(songId, findSong.getName(), true);
    }

    private SongLikeResponseDto buildResponse(Long songId, String songName, boolean liked) {
        Long likeCount = songRepository.findLikeCountBySongId(songId);

        if (likeCount == null) {
            throw new CustomException(ErrorCode.SONG_NOT_FOUND);
        }

        return SongLikeResponseDto.of(songId, songName, liked, likeCount);
    }
}
