package com.example.peachmusic.domain.song.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.common.model.PageResponse;
import com.example.peachmusic.domain.song.dto.request.AdminSongCreateRequestDto;
import com.example.peachmusic.domain.song.dto.request.AdminSongUpdateRequestDto;
import com.example.peachmusic.domain.song.dto.response.AdminSongCreateResponseDto;
import com.example.peachmusic.domain.song.dto.response.AdminSongUpdateResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
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
     */
    @PostMapping("/songs")
    public ResponseEntity<CommonResponse<AdminSongCreateResponseDto>> createSong(
            @Valid @RequestBody AdminSongCreateRequestDto requestDto
    ) {

        AdminSongCreateResponseDto responseDto = songAdminService.createSong(requestDto);

        return ResponseEntity.ok(CommonResponse.success("음원이 생성 되었습니다.", responseDto));
    }

    /**
     * 음원 전체 조회 API
     */
    @GetMapping("/songs")
    public ResponseEntity<PageResponse<SongSearchResponseDto>> getSongAll(
            @RequestParam(required = false) String word,
            @PageableDefault(size = 10, sort = "songId", direction = Sort.Direction.ASC) Pageable pageable
    ) {

        Page<SongSearchResponseDto> responseDtoPage = songAdminService.getSongAll(word, pageable);

        return ResponseEntity.ok(PageResponse.success("음원 목록 조회에 성공 했습니다", responseDtoPage));
    }

    /**
     * 음원 정보 수정 API
     */
    @PatchMapping("/songs/{songId}")
    public ResponseEntity<CommonResponse<AdminSongUpdateResponseDto>> updateSong(
            @PathVariable("songId") Long songId,
            @Valid @RequestBody AdminSongUpdateRequestDto requestDto
    ) {

        AdminSongUpdateResponseDto responseDto = songAdminService.updateSong(requestDto, songId);

        return ResponseEntity.ok(CommonResponse.success("음원 정보가 수정 되었습니다.", responseDto));
    }

    /**
     * 음원 삭제 (비활성화) API
     */
    @DeleteMapping("/songs/{songId}")
    public ResponseEntity<CommonResponse> deleteSong(
            @PathVariable("songId") Long songId
    ) {

        songAdminService.deleteSong(songId);

        return ResponseEntity.ok(CommonResponse.success("음원이 비활성화 되었습니다."));
    }

    /**
     * 음원 복구 (활성화) API
     */
    @PatchMapping("/songs/{songId}/restore")
    public ResponseEntity<CommonResponse> restoreSong(
            @PathVariable("songId") Long songId
    ) {

        songAdminService.restoreSong(songId);

        return ResponseEntity.ok(CommonResponse.success("음원이 활성화 되었습니다."));
    }
}
