package com.example.peachmusic.domain.song.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.song.model.response.SongGetDetailResponseDto;
import com.example.peachmusic.domain.song.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SongController {

    private final SongService songService;

    /**
     * 음원 단건 조회 API
     * @param songId
     * @return
     */
    @GetMapping("/songs/{songId}")
    public ResponseEntity<CommonResponse<SongGetDetailResponseDto>> getSong(
            @PathVariable("songId") Long songId
    ) {

        // 1. 서비스 레이어로 songId 전달 및 음원 단건 조회 로직 실행
        SongGetDetailResponseDto responseDto = songService.getSong(songId);

        // 2. 서비스 레이어가 반환 해준 데이터 공통 응답 객체로 감싸줌
        CommonResponse<SongGetDetailResponseDto> commonResponse = new CommonResponse<>(true, "음원 조회에 성공 했습니다.", responseDto);

        // 3. ResponseEntity로 Response Body와 응답 상태코드 정의
        return new ResponseEntity<>(commonResponse, HttpStatus.OK);

    }
}
