package com.example.peachmusic.domain.playlistSong.controller;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.playlist.dto.response.PlaylistGetSongResponseDto;
import com.example.peachmusic.domain.playlistSong.dto.request.PlaylistSongAddRequestDto;
import com.example.peachmusic.domain.playlistSong.dto.request.PlaylistSongDeleteRequestDto;
import com.example.peachmusic.domain.playlistSong.dto.response.PlaylistSongAddResponseDto;
import com.example.peachmusic.domain.playlistSong.dto.response.PlaylistSongDeleteSongResponseDto;
import com.example.peachmusic.domain.playlistSong.service.PlaylistSongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PlaylistSongController {

    private final PlaylistSongService playlistSongService;

    /**
     * 플레이리스트 음원 추가 API
     */
    @PostMapping("/playlists/{playlistId}/songs")
    public ResponseEntity<CommonResponse<PlaylistSongAddResponseDto>> addPlaylistSong(
            @PathVariable("playlistId") Long playlistId,
            @Valid @RequestBody PlaylistSongAddRequestDto requestDto,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        PlaylistSongAddResponseDto responseDto = playlistSongService.addPlaylistSong(playlistId, requestDto, authUser);

        return ResponseEntity.ok(CommonResponse.success("플레이리스트에 곡이 추가 되었습니다.", responseDto));
    }

    /**
     * 플레이리스트 음원 조회
     */
    @GetMapping("/playlists/{playlistId}")
    public ResponseEntity<CommonResponse<PlaylistGetSongResponseDto>> getPlaylistSongList(
            @PathVariable("playlistId") Long playlistId,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        PlaylistGetSongResponseDto responseDto = playlistSongService.getPlaylistSongList(playlistId, authUser);

        return ResponseEntity.ok(CommonResponse.success("플레이리스트 조회에 성공했습니다.", responseDto));
    }

    /**
     * 플레이리스트의 음원 삭제 API
     */
    @DeleteMapping("/playlists/{playlistId}/songs")
    public ResponseEntity<CommonResponse<PlaylistSongDeleteSongResponseDto>> deletePlaylistSong(
            @PathVariable("playlistId") Long playlistId,
            @Valid @RequestBody PlaylistSongDeleteRequestDto requestDto,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        PlaylistSongDeleteSongResponseDto responseDto = playlistSongService.deletePlaylistSong(playlistId, requestDto, authUser);

        return ResponseEntity.ok(CommonResponse.success("플레이리스트에서 곡이 삭제 되었습니다.", responseDto));
    }
}
