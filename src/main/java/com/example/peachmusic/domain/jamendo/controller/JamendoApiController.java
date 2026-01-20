package com.example.peachmusic.domain.jamendo.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.jamendo.service.JamendoSongService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class JamendoApiController {

    private final JamendoSongService jamendoSongService;

    @GetMapping("/admin/jamendo/tracks/import/initial")
    public CommonResponse<Void> importInitJamendo(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        jamendoSongService.importInitJamendo(startDate, endDate);
        return CommonResponse.success("기간 : " + startDate + " - " + endDate + " 데이터 적재 성공");
    }
}