package com.example.peachmusic.domain.playlistSong.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.playlistSong.model.request.PlaylistSongAddRequestDto;
import com.example.peachmusic.domain.playlistSong.model.response.PlaylistSongAddResponseDto;
import com.example.peachmusic.domain.playlistSong.service.PlaylistSongService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PlaylistSongController {

    private final PlaylistSongService playlistSongService;

    /**
     * 플레이리스트 음원 추가 API
     * @param playlistId
     * @param requestDto
     * @return
     */
    @PostMapping("/playlists/{playlistId}/songs/{userId}")
    public ResponseEntity<CommonResponse<PlaylistSongAddResponseDto>> addPlaylistSong(
            @PathVariable("playlistId") Long playlistId,
            @RequestBody PlaylistSongAddRequestDto requestDto
            ) {
        PlaylistSongAddResponseDto responseDto = playlistSongService.addPlaylistSong(playlistId, requestDto);

        CommonResponse<PlaylistSongAddResponseDto> commonResponse = new CommonResponse<>(true, "플레이리스트에 곡이 추가 되었습니다.", responseDto);

        return new ResponseEntity<>(commonResponse, HttpStatus.OK);
    }
}
