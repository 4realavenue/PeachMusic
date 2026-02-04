package com.example.peachmusic.domain.song.controller;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.domain.song.dto.response.SongGetDetailResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
import com.example.peachmusic.domain.song.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SongController {

    private final SongService songService;

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
     * 음원 검색
     * - Keyset 페이징 적용
     * - 좋아요 많은 순이 기본 정렬
     * @param word 검색어
     * @param sortType 정렬 기준
     * @param direction 정렬 방향
     * @param lastId 커서 - 마지막 앨범 ID
     * @param lastLike 커서 - 마지막 좋아요 수
     * @param lastName 커서 - 마지막 앨범 이름
     * @return 앨범 검색 결과, 다음 데이터 있는지 여부, 커서
     */
    @GetMapping("/search/songs")
    public ResponseEntity<CommonResponse<KeysetResponse<SongSearchResponseDto>>> searchSong(
            @RequestParam String word,
            @RequestParam(defaultValue = "LIKE") SortType sortType,
            @RequestParam(required = false) SortDirection direction,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false) Long lastLike,
            @RequestParam(required = false) String lastName
    ) {
        KeysetResponse<SongSearchResponseDto> result = songService.searchSongPage(word, sortType, direction, lastId, lastLike, lastName);

        return ResponseEntity.ok(CommonResponse.success("음원 검색이 완료되었습니다.", result));
    }

    /**
     * 음원 재생
     */
    @GetMapping("/songs/{songId}/play")
    public ResponseEntity<CommonResponse> playSong(
            @PathVariable Long songId,
            @RequestParam LocalDate currentDate
            ) {
        songService.play(songId, currentDate);
        return ResponseEntity.ok(CommonResponse.success("음원 재생에 성공했습니다."));
    }
}
