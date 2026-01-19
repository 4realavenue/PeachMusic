package com.example.peachmusic.domain.songLike.controller;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.songLike.model.response.SongLikeResponseDto;
import com.example.peachmusic.domain.songLike.service.SongLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
     */
    @PostMapping("/songs/{songId}/likes")
    public ResponseEntity<CommonResponse<SongLikeResponseDto>> likeSong(
            @PathVariable("songId") Long songId,
            @AuthenticationPrincipal AuthUser authUser
            ) {

        SongLikeResponseDto responseDto = songLikeService.likeSong(authUser, songId);

        return ResponseEntity.ok(CommonResponse.success("음원 좋아요 토글 성공", responseDto));
    }
}
