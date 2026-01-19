package com.example.peachmusic.domain.playlist.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.playlist.model.request.PlaylistCreateRequestDto;
import com.example.peachmusic.domain.playlist.model.request.PlaylistUpdateRequestDto;
import com.example.peachmusic.domain.playlist.model.response.PlaylistCreateResponseDto;
import com.example.peachmusic.domain.playlist.model.response.PlaylistGetListResponseDto;
import com.example.peachmusic.domain.playlist.model.response.PlaylistGetSongResponseDto;
import com.example.peachmusic.domain.playlist.model.response.PlaylistUpdateResponseDto;
import com.example.peachmusic.domain.playlist.service.PlaylistService;
import lombok.RequiredArgsConstructor;
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
     *
     * @param requestDto
     * @param userId     todo 인증/인가 들어오면 로그인 한 유저의 정보 받아오도록 수정 예정
     * @return
     */
    @PostMapping("/playlists/{userId}")
    public ResponseEntity<CommonResponse<PlaylistCreateResponseDto>> createPlaylist(
            @RequestBody PlaylistCreateRequestDto requestDto,
            @PathVariable Long userId
    ) {

        PlaylistCreateResponseDto responseDto = playlistService.createPlaylist(requestDto, userId);

        return ResponseEntity.ok(CommonResponse.success("플레이리스트가 생성 되었습니다.", responseDto));
    }

    /**
     * 플레이리스트 목록 조회 API
     *
     * @param userId todo 인증/인가 들어오면 로그인 한 유저의 정보 받아오도록 수정 예정
     * @return
     */
    @GetMapping("/playlists/{userId}")
    public ResponseEntity<CommonResponse<List<PlaylistGetListResponseDto>>> getPlaylistAll(
            @PathVariable Long userId
    ) {

        List<PlaylistGetListResponseDto> responseDtoList = playlistService.getPlaylistAll(userId);

        return ResponseEntity.ok(CommonResponse.success("플레이리스트가 조회 되었습니다", responseDtoList));
    }

    /**
     * todo url 수정 필요함 (목록 조회랑 url이 생긴게 비슷해서 에러나네)
     * 플레이리스트 음원 조회
     *
     * @param playlistId
     * @return
     */
    @GetMapping("/playlists/{playlistId}/a")
    public ResponseEntity<CommonResponse<PlaylistGetSongResponseDto>> getPlaylistSongList(
            @PathVariable("playlistId") Long playlistId
    ) {

        PlaylistGetSongResponseDto responseDto = playlistService.getPlaylistSongList(playlistId);

        return ResponseEntity.ok(CommonResponse.success("플레이리스트 조회에 성공했습니다.", responseDto));
    }

    /**
     * 플레이리스트 정보 수정 API
     *
     * @param playlistId
     * @param requestDto
     * @return
     */
    @PutMapping("/playlists/{playlistId}")
    public ResponseEntity<CommonResponse<PlaylistUpdateResponseDto>> updatePlaylist(
            @PathVariable("playlistId") Long playlistId,
            @RequestBody PlaylistUpdateRequestDto requestDto
    ) {

        PlaylistUpdateResponseDto responseDto = playlistService.updatePlaylist(playlistId, requestDto);

        return ResponseEntity.ok(CommonResponse.success("플레이리스트 이름이 수정 되었습니다.", responseDto));
    }

    /**
     * 플레이리스트 삭제 API
     *
     * @param playlistId
     * @return
     */
    @DeleteMapping("/playlists/{playlistId}")
    public ResponseEntity<CommonResponse> deletePlaylist(
            @PathVariable("playlistId") Long playlistId
    ) {

        playlistService.deletePlaylist(playlistId);

        return ResponseEntity.ok(CommonResponse.success("플레이리스트가 삭제 되었습니다.", null));
    }


}
