package com.example.peachmusic.domain.song.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.domain.song.dto.request.AdminSongCreateRequestDto;
import com.example.peachmusic.domain.song.dto.request.AdminSongUpdateRequestDto;
import com.example.peachmusic.domain.song.dto.response.AdminSongAudioUpdateResponseDto;
import com.example.peachmusic.domain.song.dto.response.AdminSongCreateResponseDto;
import com.example.peachmusic.domain.song.dto.response.AdminSongUpdateResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
import com.example.peachmusic.domain.song.service.SongAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SongAdminController {

    private final SongAdminService songAdminService;

    /**
     * 음원 생성 API
     */
    @PostMapping("/admin/songs")
    public ResponseEntity<CommonResponse<AdminSongCreateResponseDto>> createSong(
            @RequestPart("request") @Valid AdminSongCreateRequestDto requestDto,
            @RequestPart("audio") MultipartFile audio
    ) {
        AdminSongCreateResponseDto responseDto = songAdminService.createSong(requestDto, audio);

        return ResponseEntity.ok(CommonResponse.success("음원이 생성 되었습니다.", responseDto));
    }

    /**
     * 음원 전체 조회 API
     */
    @GetMapping("/admin/songs")
    public ResponseEntity<CommonResponse<KeysetResponse<SongSearchResponseDto>>> getSongList(
            @RequestParam(required = false) String word,
            @RequestParam(required = false) Long lastId
    ) {
        KeysetResponse<SongSearchResponseDto> responseDtoPage = songAdminService.getSongList(word, lastId);

        return ResponseEntity.ok(CommonResponse.success("음원 목록 조회에 성공했습니다", responseDtoPage));
    }

    /**
     * 음원 기본 정보 수정 API
     */
    @PatchMapping("/admin/songs/{songId}")
    public ResponseEntity<CommonResponse<AdminSongUpdateResponseDto>> updateSong(
            @PathVariable("songId") Long songId,
            @Valid @RequestBody AdminSongUpdateRequestDto requestDto
    ) {
        AdminSongUpdateResponseDto responseDto = songAdminService.updateSong(requestDto, songId);

        return ResponseEntity.ok(CommonResponse.success("음원 정보가 수정되었습니다.", responseDto));
    }

    /**
     * 음원 파일 수정 API
     */
    @PatchMapping("/admin/songs/{songId}/audio")
    public ResponseEntity<CommonResponse<AdminSongAudioUpdateResponseDto>> updateAudio(
            @PathVariable("songId") Long songId,
            @RequestParam MultipartFile audio
    ) {
        AdminSongAudioUpdateResponseDto responseDto = songAdminService.updateAudio(songId, audio);

        return ResponseEntity.ok(CommonResponse.success("음원 파일이 수정되었습니다.", responseDto));
    }

    /**
     * 음원 삭제 (비활성화) API
     */
    @DeleteMapping("/admin/songs/{songId}")
    public ResponseEntity<CommonResponse<Void>> deleteSong(
            @PathVariable("songId") Long songId
    ) {
        songAdminService.deleteSong(songId);

        return ResponseEntity.ok(CommonResponse.success("음원이 비활성화 되었습니다."));
    }

    /**
     * 음원 복구 (활성화) API
     */
    @PatchMapping("/admin/songs/{songId}/restore")
    public ResponseEntity<CommonResponse<Void>> restoreSong(
            @PathVariable("songId") Long songId
    ) {
        songAdminService.restoreSong(songId);

        return ResponseEntity.ok(CommonResponse.success("음원이 활성화 되었습니다."));
    }
}
