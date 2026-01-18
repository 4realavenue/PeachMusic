package com.example.peachmusic.domain.songLike.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.songLike.model.response.SongLikeResponseDto;
import com.example.peachmusic.domain.songLike.service.SongLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SongLikeController {
    
    private final SongLikeService songLikeService;

    /**
     * 음원 좋아요/좋아요취소 기능 API
     * todo 인증/인가 들어오면 로그인 한 user 식별 로직 추가 예정
     * @param songId
     * @param userId
     * @return
     */
    @PostMapping("/songs/{songId}/likes/{userId}")
    public ResponseEntity<CommonResponse<SongLikeResponseDto>> likeSong(
            @PathVariable("songId") Long songId,
            @PathVariable("userId") Long userId
    ) {

        SongLikeResponseDto responseDto = songLikeService.likeSong(userId, songId);

        CommonResponse<SongLikeResponseDto> commonResponse = new CommonResponse<>(true, "음원 좋아요 토글 성공", responseDto);

        return new ResponseEntity<>(commonResponse, HttpStatus.OK);
    }
}
