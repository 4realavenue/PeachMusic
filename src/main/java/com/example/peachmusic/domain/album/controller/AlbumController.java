package com.example.peachmusic.domain.album.controller;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.domain.album.dto.response.AlbumGetDetailResponseDto;
import com.example.peachmusic.domain.album.dto.response.AlbumSearchResponseDto;
import com.example.peachmusic.domain.album.service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public ResponseEntity<CommonResponse<AlbumGetDetailResponseDto>> getAlbumDetail(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("albumId") Long albumId
    ) {
        AlbumGetDetailResponseDto responseDto = albumService.getAlbumDetail(authUser, albumId);

        return ResponseEntity.ok(CommonResponse.success("앨범 조회에 성공했습니다.", responseDto));
    }

    /**
     * 앨범 검색
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
    @GetMapping("/search/albums")
    public ResponseEntity<CommonResponse<KeysetResponse<AlbumSearchResponseDto>>> searchAlbum(
            @RequestParam String word,
            @RequestParam(defaultValue = "LIKE") SortType sortType,
            @RequestParam(required = false) SortDirection direction,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false) Long lastLike,
            @RequestParam(required = false) String lastName
    ) {
        KeysetResponse<AlbumSearchResponseDto> responseDto = albumService.searchAlbumPage(word, sortType, direction, lastId, lastLike, lastName);

        return ResponseEntity.ok(CommonResponse.success("앨범 검색이 완료되었습니다.", responseDto));
    }
}
