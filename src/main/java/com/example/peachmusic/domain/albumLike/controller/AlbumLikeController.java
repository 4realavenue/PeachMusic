package com.example.peachmusic.domain.albumLike.controller;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.albumLike.dto.response.AlbumLikeResponseDto;
import com.example.peachmusic.domain.albumLike.service.AlbumLikeService;
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

        return ResponseEntity.ok(CommonResponse.success("앨범 좋아요 토글 성공", responseDto));
    }
}
