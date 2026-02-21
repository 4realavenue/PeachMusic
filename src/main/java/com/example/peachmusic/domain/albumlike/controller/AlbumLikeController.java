package com.example.peachmusic.domain.albumlike.controller;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.albumlike.dto.request.AlbumLikeCheckRequestDto;
import com.example.peachmusic.domain.albumlike.dto.response.AlbumLikeCheckResponseDto;
import com.example.peachmusic.domain.albumlike.dto.response.AlbumLikeResponseDto;
import com.example.peachmusic.domain.albumlike.service.AlbumLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AlbumLikeController {

    private final AlbumLikeService albumLikeService;

    /**
     * 앨범 좋아요 토글 API
     *
     * @param authUser 인증된 사용자 정보
     * @param albumId 좋아요 토글할 앨범 ID
     * @return 토글 처리 결과(최종 좋아요 상태 및 좋아요 수)
     */
    @PostMapping("/albums/{albumId}/likes")
    public ResponseEntity<CommonResponse<AlbumLikeResponseDto>> likeAlbum(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("albumId") Long albumId) {

        AlbumLikeResponseDto responseDto = albumLikeService.likeAlbum(authUser, albumId);

        return ResponseEntity.ok(CommonResponse.success("앨범 좋아요 토글에 성공했습니다.", responseDto));
    }

    @PostMapping("/albums/likes/check")
    public ResponseEntity<CommonResponse<AlbumLikeCheckResponseDto>> checkAlbumLike(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody AlbumLikeCheckRequestDto requestDto
    ) {
        AlbumLikeCheckResponseDto responseDto = albumLikeService.checkAlbumLike(authUser, requestDto);

        return ResponseEntity.ok(CommonResponse.success("좋아요한 앨범 목록 조회에 성공했습니다.", responseDto));
    }
}
