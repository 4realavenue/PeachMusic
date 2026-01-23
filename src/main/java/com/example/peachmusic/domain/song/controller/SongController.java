package com.example.peachmusic.domain.song.controller;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.common.model.PageResponse;
import com.example.peachmusic.domain.song.dto.response.SongGetDetailResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
import com.example.peachmusic.domain.song.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(CommonResponse.success("음원 조회에 성공 했습니다.", responseDto));
    }

    /**
     * 음원 검색
     *
     * @param word     검색어
     * @param pageable 페이징 정보 - 인기순 정렬
     * @return 음원 검색 응답 DTO (음원 id, 이름, 좋아요 수)
     */
    @GetMapping("/search/songs")
    public ResponseEntity<PageResponse<SongSearchResponseDto>> searchSong(
            @RequestParam String word,
            @PageableDefault(size = 10, sort = "likeCount", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<SongSearchResponseDto> result = songService.searchSongPage(word, pageable);
        return ResponseEntity.ok(PageResponse.success("음원 검색이 완료되었습니다.", result));
    }
}
