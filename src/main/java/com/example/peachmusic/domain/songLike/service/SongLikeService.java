package com.example.peachmusic.domain.songLike.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.songLike.entity.SongLike;
import com.example.peachmusic.domain.songLike.model.response.SongLikeResponseDto;
import com.example.peachmusic.domain.songLike.repository.SongLikeRepository;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SongLikeService {

    private final SongLikeRepository songLikeRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;

    /**
     * 음원 좋아요/좋아요 취소 기능
     */
    @Transactional
    public SongLikeResponseDto likeSong(AuthUser authUser, Long songId) {

        Long userId = authUser.getUserId();

        // 1. 유저 찾아오기
        User findUser = userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 음원 찾아오기
        Song findSong = songRepository.findBySongIdAndIsDeletedFalse(songId)
                .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        // 3. 좋아요 여부를 요청한 유저와 요청받은 음원을 갖고 있는 songLike 데이터가 있는지로 판단
        boolean liked = songLikeRepository.existsSongLikeByUserAndSong(findUser, findSong);

        // 4. 만약에 songLike 데이터가 있다면 좋아요 눌렀던 것으로 판단,
        //    해당 데이터 제거 및 unlikeSong(likeCount --) 로직 실행
        if (liked) {
            songLikeRepository.deleteSongLikeByUserAndSong(findUser, findSong);
            liked = false;
            findSong.unlikeSong();
        }

        // 5. 만약에 songLike 데이터가 없다면 좋아요를 누르지 않은 상태인 것으로 판단,
        //    해당 데이터를 생성하고 저장 후 likeSong(likeCount ++) 로직 실행
        else if (!liked) {
            SongLike songLike = new SongLike(findUser, findSong);
            songLikeRepository.save(songLike);
            liked = true;
            findSong.likeSong();
        }

        return SongLikeResponseDto.from(findSong, liked);

    }
}
