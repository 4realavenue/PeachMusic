package com.example.peachmusic.domain.ranking.controller;

import com.example.peachmusic.domain.ranking.model.RankingResponseDto;
import com.example.peachmusic.domain.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/songs/ranking/Top100")
    public List<RankingResponseDto> findTop100Music(
            @RequestParam(required = false) int page,
            @RequestParam(defaultValue = "10" ) int limit
    ) {
        return rankingService.findMusicTop100(page, limit);
    }
}