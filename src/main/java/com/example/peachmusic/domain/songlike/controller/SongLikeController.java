package com.example.peachmusic.domain.songlike.controller;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.songlike.dto.request.SongLikeCheckRequestDto;
import com.example.peachmusic.domain.songlike.dto.response.SongLikeCheckResponseDto;
import com.example.peachmusic.domain.songlike.dto.response.SongLikeResponseDto;
import com.example.peachmusic.domain.songlike.service.SongLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SongLikeController {

    private final SongLikeService songLikeService;

    /**
     * 음원 좋아요/좋아요취소 기능 API
     */
    @PostMapping("/songs/{songId}/likes")
    public ResponseEntity<CommonResponse<SongLikeResponseDto>> likeSong(
            @PathVariable("songId") Long songId,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        SongLikeResponseDto responseDto = songLikeService.likeSong(authUser, songId);

        return ResponseEntity.ok(CommonResponse.success("음원 좋아요 토글에 성공했습니다.", responseDto));
    }

    @PostMapping("/songs/likes/check")
    public ResponseEntity<CommonResponse<SongLikeCheckResponseDto>> checkSongLike(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody SongLikeCheckRequestDto requestDto
    ) {
        SongLikeCheckResponseDto responseDto = songLikeService.checkSongLike(authUser, requestDto);

        return ResponseEntity.ok(CommonResponse.success("좋아요한 음원 목록 조회에 성공했습니다.", responseDto));
    }
}
