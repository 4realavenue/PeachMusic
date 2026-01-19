package com.example.peachmusic.domain.songLike.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.songLike.model.response.SongLikeResponseDto;
import com.example.peachmusic.domain.songLike.service.SongLikeService;
import lombok.RequiredArgsConstructor;
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
     *
     * @param songId
     * @param userId todo 인증/인가 들어오면 로그인 한 user 식별 로직 추가 예정
     * @return
     */
    @PostMapping("/songs/{songId}/likes/{userId}")
    public ResponseEntity<CommonResponse<SongLikeResponseDto>> likeSong(
            @PathVariable("songId") Long songId,
            // todo 인증/인가 들어오면 로그인 한 user 식별 로직 추가 예정
            @PathVariable("userId") Long userId
    ) {

        SongLikeResponseDto responseDto = songLikeService.likeSong(userId, songId);

        return ResponseEntity.ok(CommonResponse.success("음원 좋아요 토글 성공", responseDto));
    }
}
