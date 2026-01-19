package com.example.peachmusic.domain.song.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.common.model.PageResponse;
import com.example.peachmusic.domain.song.model.request.AdminSongCreateRequestDto;
import com.example.peachmusic.domain.song.model.request.AdminSongUpdateRequestDto;
import com.example.peachmusic.domain.song.model.response.AdminSongCreateResponseDto;
import com.example.peachmusic.domain.song.model.response.AdminSongGetAllResponseDto;
import com.example.peachmusic.domain.song.model.response.AdminSongUpdateResponseDto;
import com.example.peachmusic.domain.song.service.SongAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
            @Valid @RequestBody AdminSongCreateRequestDto requestDto
    ) {

        // 1. 서비스 레이어로 요청 Dto 전달 및 음원 생성 로직 수행
        AdminSongCreateResponseDto responseDto = songAdminService.createSong(requestDto);

        // 2. ResponseEntity로 Response Body 및 응답 상태코드 정의
        return ResponseEntity.ok(CommonResponse.success("음원이 생성 되었습니다.", responseDto));
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

        // 1. 서비스 레이어로 페이지 설정 전달 및 음원 전체 조회 로직 수행
        Page<AdminSongGetAllResponseDto> responseDtoPage = songAdminService.getSongAll(pageable);

        // 2. ResponseEntity로 Response Body 및 응답 상태코드 정의
        return ResponseEntity.ok(PageResponse.success("음원 목록 조회에 성공 했습니다", responseDtoPage));
    }

    /**
     * 음원 정보 수정 API
     *
     * @param songId
     * @param requestDto
     * @return
     */
    @PutMapping("/songs/{songId}")
    public ResponseEntity<CommonResponse<AdminSongUpdateResponseDto>> updateSong(
            @PathVariable("songId") Long songId,
            @Valid @RequestBody AdminSongUpdateRequestDto requestDto
    ) {

        // 1. 서비스 레이어로 요청 받은 songId와 요청 dto 전달 및 음원 정보 수정 로직 실행
        AdminSongUpdateResponseDto responseDto = songAdminService.updateSong(requestDto, songId);

        // 2. ResponseEntity로 Response Body 및 응답 상태코드 정의
        return ResponseEntity.ok(CommonResponse.success("음원 정보가 수정 되었습니다.", responseDto));
    }

    /**
     * 음원 삭제 (비활성화) API
     */
    @DeleteMapping("/songs/{songId}")
    public ResponseEntity<CommonResponse> deleteSong(
            @PathVariable("songId") Long songId
    ) {

        // 1. 서비스 레이어로 songId 전달 및 음원 삭제 로직 실행
        songAdminService.deleteSong(songId);

        // 2. ResponseEntity로 Response Body 및 응답 상태코드 정의
        return ResponseEntity.ok(CommonResponse.success("음원이 비활성화 되었습니다.", null));
    }

    /**
     * 음원 복구 (활성화) API
     */
    @PatchMapping("/songs/{songId}/restore")
    public ResponseEntity<CommonResponse> restoreSong(
            @PathVariable("songId") Long songId
    ) {

        // 1. 서비스 레이어로 songId 전달 및 음원 복구 로직 실행
        songAdminService.restoreSong(songId);

        // 2. ResponseEntity로 Response Body 및 응답 상태코드 정의
        return ResponseEntity.ok(CommonResponse.success("음원이 활성화 되었습니다.", null));
    }
}
