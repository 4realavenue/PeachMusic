package com.example.peachmusic.domain.song.controller;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.model.*;
import com.example.peachmusic.domain.song.dto.response.SongArtistDetailResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongGetDetailResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongPlayResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
import com.example.peachmusic.domain.song.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SongController {

    private final SongService songService;

    /**
     * 음원 전체 조회 API
     */
    @GetMapping("/songs")
    public ResponseEntity<CommonResponse<KeysetResponse<SongSearchResponseDto>>> getSongList(
            @RequestParam(defaultValue = "LIKE") SortType sortType,
            @RequestParam(required = false) SortDirection direction,
            @ModelAttribute CursorParam cursor
    ) {
        KeysetResponse<SongSearchResponseDto> responseDtoPage = songService.getSongList(sortType, direction, cursor);

        return ResponseEntity.ok(CommonResponse.success("음원 전체 조회에 성공했습니다.", responseDtoPage));
    }

    /**
     * 음원 단건 조회 API
     */
    @GetMapping("/songs/{songId}")
    public ResponseEntity<CommonResponse<SongGetDetailResponseDto>> getSong(
            @PathVariable("songId") Long songId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        SongGetDetailResponseDto responseDto = songService.getSong(songId, authUser);

        return ResponseEntity.ok(CommonResponse.success("음원 조회에 성공했습니다.", responseDto));
    }

    /**
     * 아티스트 단건 조회 시 음원 전체 보기
     */
    @GetMapping("/artists/{artistId}/songs")
    public ResponseEntity<CommonResponse<KeysetResponse<SongArtistDetailResponseDto>>> getArtistSongs(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long artistId,
            @ModelAttribute CursorParam cursor
    ) {
        KeysetResponse<SongArtistDetailResponseDto> responseDto = songService.getArtistSongs(authUser, artistId, cursor);
        return ResponseEntity.ok(CommonResponse.success("아티스트의 음원 전체 조회에 성공했습니다.", responseDto));
    }

    /**
     * 음원 검색
     * - Keyset 페이징 적용
     * - 좋아요 많은 순이 기본 정렬
     * @param condition 검색어, 정렬 기준, 정렬 방향, 커서
     * @return 앨범 검색 결과, 다음 데이터 있는지 여부, 커서
     */
    @GetMapping("/search/songs")
    public ResponseEntity<CommonResponse<KeysetResponse<SongSearchResponseDto>>> searchSong(
            @Valid @ModelAttribute SearchConditionParam condition,
            @ModelAttribute CursorParam cursor
    ) {
        KeysetResponse<SongSearchResponseDto> responseDto = songService.searchSongPage(condition, cursor);

        return ResponseEntity.ok(CommonResponse.success("음원 검색이 완료되었습니다.", responseDto));
    }

    /**
     * 음원 재생
     */
    @GetMapping("/songs/{songId}/play")
    public ResponseEntity<CommonResponse<SongPlayResponseDto>> playSong(
            @PathVariable Long songId
    ) {
        SongPlayResponseDto responseDto = songService.playSong(songId);

        return ResponseEntity.ok(CommonResponse.success("음원 재생에 성공했습니다.", responseDto));
    }
}
