package com.example.peachmusic.domain.playlist.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.playlist.model.request.PlaylistCreateRequestDto;
import com.example.peachmusic.domain.playlist.model.response.PlaylistCreateResponseDto;
import com.example.peachmusic.domain.playlist.model.response.PlaylistGetAllResponseDto;
import com.example.peachmusic.domain.playlist.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PlaylistController {

    private final PlaylistService playlistService;

    /**
     * 플레이리스트 생성
     * @param requestDto
     * @param userId
     * @return
     */
    @PostMapping("/playlists/{userId}")
    public ResponseEntity<CommonResponse<PlaylistCreateResponseDto>> createPlaylist(
            @RequestBody PlaylistCreateRequestDto requestDto,
            @PathVariable Long userId
            ) {

        // todo 인증/인가 들어오면 로그인 한 유저의 정보 받아오도록 수정 예정
        PlaylistCreateResponseDto responseDto = playlistService.createPlaylist(requestDto, userId);

        CommonResponse<PlaylistCreateResponseDto> commonResponse = new CommonResponse<>(true, "플레이리스트가 생성 되었습니다.", responseDto);

        return new ResponseEntity<>(commonResponse, HttpStatus.CREATED);
    }

    /**
     * 플레이리스트 목록 조회 API
     * @param userId
     * @return
     */
    @GetMapping("/playlists/{userId}")
    public ResponseEntity<CommonResponse<PlaylistGetAllResponseDto>> getPlaylistAll(
            @PathVariable Long userId
    ) {

        PlaylistGetAllResponseDto responseDtoList = playlistService.getPlaylistAll(userId);

        CommonResponse<PlaylistGetAllResponseDto> commonResponse = new CommonResponse<>(true, "플레이리스트가 조회 되었습니다", responseDtoList);

        return new ResponseEntity<>(commonResponse, HttpStatus.OK);

    }


}
