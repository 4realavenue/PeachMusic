package com.example.peachmusic.domain.artistLike.controller;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.artistLike.model.response.ArtistLikeResponseDto;
import com.example.peachmusic.domain.artistLike.service.ArtistLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ArtistLikeController {

    private final ArtistLikeService artistLikeService;

    /**
     * 아티스트 좋아요 토글 API
     *
     * @param authUser 인증된 사용자 정보
     * @param artistId 좋아요 토글할 아티스트 ID
     * @return 토글 처리 결과(최종 좋아요 상태 및 좋아요 수)
     */
    @PostMapping("/artists/{artistId}/likes")
    public ResponseEntity<CommonResponse<ArtistLikeResponseDto>> likeArtist(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("artistId") Long artistId) {

        ArtistLikeResponseDto responseDto = artistLikeService.likeArtist(authUser, artistId);

        return ResponseEntity.ok(CommonResponse.success("아티스트 좋아요 토글 성공", responseDto));
    }
}
