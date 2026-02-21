package com.example.peachmusic.domain.album.controller;

import com.example.peachmusic.common.model.*;
import com.example.peachmusic.domain.album.dto.response.AlbumArtistDetailResponseDto;
import com.example.peachmusic.domain.album.dto.response.AlbumGetDetailResponseDto;
import com.example.peachmusic.domain.album.dto.response.AlbumSearchResponseDto;
import com.example.peachmusic.domain.album.service.AlbumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
     * 아티스트 단건 조회 시 앨범 전체 보기
     */
    @GetMapping("/artists/{artistId}/albums")
    public ResponseEntity<CommonResponse<KeysetResponse<AlbumArtistDetailResponseDto>>> getArtistAlbums(
            @PathVariable Long artistId,
            @ModelAttribute CursorParam cursor
    ) {
        KeysetResponse<AlbumArtistDetailResponseDto> responseDto = albumService.getArtistAlbums(artistId, cursor);
        return ResponseEntity.ok(CommonResponse.success("아티스트의 앨범 전체 조회에 성공했습니다.", responseDto));
    }

    /**
     * 앨범 검색
     * - Keyset 페이징 적용
     * - 좋아요 많은 순이 기본 정렬
     * @param condition 검색어, 정렬 기준, 정렬 방향, 커서
     * @return 앨범 검색 결과, 다음 데이터 있는지 여부, 커서
     */
    @GetMapping("/search/albums")
    public ResponseEntity<CommonResponse<KeysetResponse<AlbumSearchResponseDto>>> searchAlbum(
            @Valid @ModelAttribute SearchConditionParam condition,
            @ModelAttribute CursorParam cursor
    ) {
        KeysetResponse<AlbumSearchResponseDto> responseDto = albumService.searchAlbumPage(condition, cursor);

        return ResponseEntity.ok(CommonResponse.success("앨범 검색이 완료되었습니다.", responseDto));
    }
}
