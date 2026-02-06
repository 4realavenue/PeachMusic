package com.example.peachmusic.domain.song.controller;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.song.dto.response.SongRecommendationResponseDto;
import com.example.peachmusic.domain.song.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RecommendationController {
    private final RecommendationService recommendationService;

    @GetMapping("/songs/recommendation")
    public ResponseEntity<CommonResponse<List<SongRecommendationResponseDto>>> getRecommendedSongs(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        List<SongRecommendationResponseDto> result = recommendationService.getRecommendedSongSlice(authUser, pageable);
        return ResponseEntity.ok(CommonResponse.success("음원 추천이 완료되었습니다.", result));
    }
}