package com.example.peachmusic.domain.artist.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.artist.dto.response.ArtistGetDetailResponseDto;
import com.example.peachmusic.common.model.PageResponse;
import com.example.peachmusic.domain.artist.service.ArtistService;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ArtistController {

    private final ArtistService artistService;

    /**
     * 아티스트 단건 조회 API
     * @param artistId 조회할 아티스트 ID
     * @return 조회한 아티스트 정보
     */
    @GetMapping("/artists/{artistId}")
    public ResponseEntity<CommonResponse<ArtistGetDetailResponseDto>> getArtistDetail(@PathVariable("artistId") Long artistId) {

       ArtistGetDetailResponseDto responseDto = artistService.getArtistDetail(artistId);

       return ResponseEntity.ok(CommonResponse.success("아티스트 조회 성공", responseDto));
    }

    /**
     * 아티스트 검색
     * @param word 검색어
     * @param pageable 페이징 정보 - 인기순 정렬
     * @return 아티스트 검색 응답 DTO (아티스트 id, 이름, 좋아요 수)
     */
    @GetMapping("/search/artists")
    public ResponseEntity<PageResponse<ArtistSearchResponseDto>> searchArtist(
            @RequestParam String word,
            @PageableDefault(size = 10, sort = "likeCount", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ArtistSearchResponseDto> result = artistService.searchArtistPage(word, pageable);
        return ResponseEntity.ok(PageResponse.success("아티스트 검색이 완료되었습니다.", result));
    }
}
