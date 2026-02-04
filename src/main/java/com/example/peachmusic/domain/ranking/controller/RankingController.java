package com.example.peachmusic.domain.ranking.controller;

import com.example.peachmusic.domain.ranking.model.RankingDto;
import com.example.peachmusic.domain.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/songs/ranking/Top100")
    public List<RankingDto> findTop100Music(
            @RequestParam(required = false) int page,
            @RequestParam(defaultValue = "10" ) int limit
    ) {
        LocalDate currentDate = LocalDate.now();
        return rankingService.findMusicTop100(currentDate, page, limit);
    }
}