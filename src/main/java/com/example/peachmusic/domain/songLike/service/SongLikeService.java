package com.example.peachmusic.domain.songLike.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.songLike.dto.response.SongLikeResponseDto;
import com.example.peachmusic.domain.songLike.entity.SongLike;
import com.example.peachmusic.domain.songLike.repository.SongLikeRepository;
import com.example.peachmusic.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        User findUser = authUser.getUser();

        Song findSong = songRepository.findBySongIdAndIsDeletedFalse(songId)
                .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        boolean liked = songLikeRepository.existsSongLikeByUserAndSong(findUser, findSong);

        if (liked) {
            songLikeRepository.deleteSongLikeByUserAndSong(findUser, findSong);

            liked = false;

            findSong.unlikeSong();
        } else {
            SongLike songLike = new SongLike(findUser, findSong);

            songLikeRepository.save(songLike);

            liked = true;

            findSong.likeSong();
        }

        return SongLikeResponseDto.from(findSong, liked);

    }
}
