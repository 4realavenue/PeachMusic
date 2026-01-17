package com.example.peachmusic.domain.song.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.common.model.PageResponse;
import com.example.peachmusic.domain.song.model.request.AdminSongCreateRequestDto;
import com.example.peachmusic.domain.song.model.request.AdminSongUpdateRequestDto;
import com.example.peachmusic.domain.song.model.response.AdminSongCreateResponseDto;
import com.example.peachmusic.domain.song.model.response.AdminSongGetAllResponseDto;
import com.example.peachmusic.domain.song.model.response.AdminSongUpdateResponseDto;
import com.example.peachmusic.domain.song.service.SongAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class SongAdminController {

    private final SongAdminService songAdminService;

    /**
     * 음원 생성 API
     *
     * @param requestDto
     * @return
     */
    @PostMapping("/songs")
    public ResponseEntity<CommonResponse<AdminSongCreateResponseDto>> createSong(
            @RequestBody AdminSongCreateRequestDto requestDto
    ) {

        // 1. 서비스 레이어로 요청 Dto 전달 및 음원 생성 로직 수행
        AdminSongCreateResponseDto responseDto = songAdminService.createSong(requestDto);

        // 2. 서비스 레이어가 반환한 데이터를 공통 응답 객체로 감싸줌
        CommonResponse<AdminSongCreateResponseDto> commonResponse = new CommonResponse<>(true, "음원이 생성 되었습니다.", responseDto);

        // 3. ResponseEntity로 Response Body 및 응답 상태코드 정의
        return new ResponseEntity<>(commonResponse, HttpStatus.CREATED);
    }

    /**
     * 음원 전체 조회 API
     *
     * @param pageable
     * @return
     */
    @GetMapping("/songs")
    public ResponseEntity<PageResponse<AdminSongGetAllResponseDto>> getSongAll(
            @PageableDefault(size = 20, sort = "songId", direction = Sort.Direction.ASC) Pageable pageable
    ) {

        // 1. 서비스 레이어로 요청 Dto 및 Pageable 설정 전달 및 음원 전체 조회 로직 수행
        PageResponse.PageData<AdminSongGetAllResponseDto> responseDtoPageData = songAdminService.getSongAll(pageable);

        // 2. 서비스 레이어가 반환한 데이터를 공통 페이지 응답 객체로 감싸줌
        PageResponse<AdminSongGetAllResponseDto> pageResponse = new PageResponse<>(true, "음원 목록 조회에 성공 했습니다.", responseDtoPageData);

        // 3. ResponseEntity로 Response Body 및 응답 상태코드 정의
        return new ResponseEntity<>(pageResponse, HttpStatus.OK);

    }

    @PutMapping("/songs/{songId}")
    public ResponseEntity<CommonResponse<AdminSongUpdateResponseDto>> updateSong(
            @PathVariable("songId") Long songId,
            @RequestBody AdminSongUpdateRequestDto requestDto
    ) {

        // 1. 서비스 레이어로 요청 Dto 및 songId 전달 및 음원 수정 로직 실행
        AdminSongUpdateResponseDto responseDto = songAdminService.updateSong(requestDto, songId);

        // 2. 서비스 레이어가 반환한 데이터를 공통 응답 객체로 감싸줌
        CommonResponse<AdminSongUpdateResponseDto> commonResponse = new CommonResponse<>(true, "음원 정보가 수정 되었습니다.", responseDto);

        // 3. ResponseEntity로 Response Body 및 응답 상태코드 정의
        return new ResponseEntity<>(commonResponse, HttpStatus.OK);

    }
}
