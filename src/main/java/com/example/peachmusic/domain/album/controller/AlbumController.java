package com.example.peachmusic.domain.album.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.album.model.response.AlbumGetDetailResponseDto;
import com.example.peachmusic.common.model.PageResponse;
import com.example.peachmusic.domain.album.model.response.AlbumSearchResponse;
import com.example.peachmusic.domain.album.service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AlbumController {

    private final AlbumService albumService;

    /**
     * 앨범 단건 조회 API
     * @param albumId 조회할 앨범 ID
     * @return 조회한 앨범 정보
     */
    @GetMapping("/albums/{albumId}")
    public ResponseEntity<CommonResponse<AlbumGetDetailResponseDto>> getAlbumDetail(@PathVariable("albumId") Long albumId) {

        AlbumGetDetailResponseDto responseDto = albumService.getAlbumDetail(albumId);

        return ResponseEntity.ok(CommonResponse.success("앨범 조회 성공", responseDto));
    }

    /**
     * 앨범 검색
     * @param word 검색어
     * @param pageable 페이징 정보 - 인기순 정렬
     * @return 앨범 검색 응답 DTO (앨범 id, 이름, 발매일, 이미지, 좋아요 수)
     */
    @GetMapping("/search/albums")
    public ResponseEntity<PageResponse<AlbumSearchResponse>> searchAlbum(
            @RequestParam String word,
            @PageableDefault(size = 10, sort = "likeCount", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AlbumSearchResponse> result = albumService.searchAlbumPage(word, pageable);
        return ResponseEntity.ok(PageResponse.success("앨범 검색이 완료되었습니다.", result));
    }
}
