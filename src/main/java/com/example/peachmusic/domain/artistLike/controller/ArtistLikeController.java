package com.example.peachmusic.domain.artistLike.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.artistLike.model.response.ArtistLikeResponseDto;
import com.example.peachmusic.domain.artistLike.service.ArtistLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ArtistLikeController {

    private final ArtistLikeService artistLikeService;

    /**
     * 아티스트 좋아요 토글 API
     * JWT 적용 전 단계로, 요청 헤더에서 사용자 식별 정보를 임시로 전달받는다.
     *
     * @param userId 요청 헤더에 전달되는 사용자 ID (JWT 적용 전까지 임시 사용)
     * @param artistId 좋아요 토글할 아티스트 ID
     * @return 토글 처리 결과(최종 좋아요 상태 및 좋아요 수)
     */
    @PostMapping("/artists/{artistId}/likes")
    public ResponseEntity<CommonResponse<ArtistLikeResponseDto>> likeArtist(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable("artistId") Long artistId) {

        ArtistLikeResponseDto responseDto = artistLikeService.likeArtist(userId, artistId);

        return ResponseEntity.ok(CommonResponse.success("아티스트 좋아요 토글 성공", responseDto));
    }
}
