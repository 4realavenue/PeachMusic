package com.example.peachmusic.domain.jamendo.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.jamendo.service.JamendoSongService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class JamendoApiController {

    private final JamendoSongService jamendoSongService;

    @GetMapping("/admin/jamendo/tracks/import/initial")
    public CommonResponse<Void> importInitJamendo(
            @RequestParam String type
    ) {
        jamendoSongService.importInitJamendo(type);
        return CommonResponse.success("초기" + type + "데이터 적재 성공");
    }
}