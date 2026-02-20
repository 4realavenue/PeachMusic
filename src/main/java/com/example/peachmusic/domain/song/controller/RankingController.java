package com.example.peachmusic.domain.song.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.song.dto.response.RankingResponseDto;
import com.example.peachmusic.domain.song.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/songs/ranking/Top100")
    public ResponseEntity<CommonResponse<List<RankingResponseDto>>> findTop100Music(
    ) {
        List<RankingResponseDto> result = rankingService.findMusicTop100();
        return ResponseEntity.ok(CommonResponse.success("조회 완료했습니다.", result));
    }
}