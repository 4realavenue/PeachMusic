package com.example.peachmusic.domain.playlist.controller;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.playlist.dto.request.PlaylistCreateRequestDto;
import com.example.peachmusic.domain.playlist.dto.request.PlaylistUpdateRequestDto;
import com.example.peachmusic.domain.playlist.dto.response.PlaylistCreateResponseDto;
import com.example.peachmusic.domain.playlist.dto.response.PlaylistGetListResponseDto;
import com.example.peachmusic.domain.playlist.dto.response.PlaylistImageUpdateResponseDto;
import com.example.peachmusic.domain.playlist.dto.response.PlaylistUpdateResponseDto;
import com.example.peachmusic.domain.playlist.service.PlaylistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PlaylistController {

    private final PlaylistService playlistService;

    /**
     * 플레이리스트 생성
     */
    @PostMapping("/playlists")
    public ResponseEntity<CommonResponse<PlaylistCreateResponseDto>> createPlaylist(
            @RequestPart("request") @Valid PlaylistCreateRequestDto requestDto,
            @RequestPart(value = "playlistImage", required = false) MultipartFile playlistImage,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        PlaylistCreateResponseDto responseDto = playlistService.createPlaylist(requestDto, playlistImage, authUser);

        return ResponseEntity.ok(CommonResponse.success("플레이리스트가 생성 되었습니다.", responseDto));
    }

    /**
     * 플레이리스트 목록 조회 API
     */
    @GetMapping("/playlists")
    public ResponseEntity<CommonResponse<List<PlaylistGetListResponseDto>>> getPlaylistAll(
            @AuthenticationPrincipal AuthUser authUser
    ) {

        List<PlaylistGetListResponseDto> responseDtoList = playlistService.getPlaylistAll(authUser);

        return ResponseEntity.ok(CommonResponse.success("플레이리스트가 조회 되었습니다", responseDtoList));
    }

    /**
     * 플레이리스트 기본 정보 수정 API
     */
    @PatchMapping("/playlists/{playlistId}")
    public ResponseEntity<CommonResponse<PlaylistUpdateResponseDto>> updatePlaylist(
            @PathVariable("playlistId") Long playlistId,
            @Valid @RequestBody PlaylistUpdateRequestDto requestDto,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        PlaylistUpdateResponseDto responseDto = playlistService.updatePlaylist(playlistId, requestDto, authUser);

        return ResponseEntity.ok(CommonResponse.success("플레이리스트 이름이 수정 되었습니다.", responseDto));
    }

    /**
     * 플레이리스트 이미지 수정 API
     */
    @PatchMapping("/playlists/{playlistId}/playlist-image")
    public ResponseEntity<CommonResponse<PlaylistImageUpdateResponseDto>> updatePlaylistImage(
            @PathVariable("playlistId") Long playlistId,
            @RequestParam MultipartFile playlistImage,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        PlaylistImageUpdateResponseDto responseDto = playlistService.updatePlaylistImage(playlistId, playlistImage, authUser);

        return ResponseEntity.ok(CommonResponse.success("플레이리스트 이미지가 수정되었습니다.", responseDto));
    }

    /**
     * 플레이리스트 삭제 API
     */
    @DeleteMapping("/playlists/{playlistId}")
    public ResponseEntity<CommonResponse<Void>> deletePlaylist(
            @PathVariable("playlistId") Long playlistId,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        playlistService.deletePlaylist(playlistId, authUser);

        return ResponseEntity.ok(CommonResponse.success("플레이리스트가 삭제 되었습니다."));
    }


}
