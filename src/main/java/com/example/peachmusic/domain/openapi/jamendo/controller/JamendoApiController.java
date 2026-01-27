package com.example.peachmusic.domain.openapi.jamendo.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.openapi.jamendo.dto.JamendoInitRequestDto;
import com.example.peachmusic.domain.openapi.jamendo.service.JamendoSongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class JamendoApiController {

    private final JamendoSongService jamendoSongService;

    @PostMapping("/admin/openapi/jamendo")
    public ResponseEntity<CommonResponse<Void>> importInitJamendo(
            @Valid @RequestBody JamendoInitRequestDto request
    ) {
        jamendoSongService.importInitJamendo(request);
        return ResponseEntity.ok(CommonResponse.success("기간 : " + request.getStartDate() + " - " + request.getEndDate() + " 데이터 적재 성공"));
    }
}