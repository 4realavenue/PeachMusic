package com.example.peachmusic.domain.song.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.song.model.request.SongCreateRequestDto;
import com.example.peachmusic.domain.song.model.response.SongCreateResponseDto;
import com.example.peachmusic.domain.song.service.SongAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class SongAdminController {

    private final SongAdminService songAdminService;

    @PostMapping("/songs")
    public ResponseEntity<CommonResponse<SongCreateResponseDto>> createSong(
            @RequestBody SongCreateRequestDto requestDto
            ) {

        SongCreateResponseDto responseDto = songAdminService.createSong(requestDto);

        CommonResponse<SongCreateResponseDto> commonResponse = new CommonResponse<>(true, "음원이 생성 되었습니다.", responseDto);

        return new ResponseEntity<>(commonResponse, HttpStatus.CREATED);
    }
}
