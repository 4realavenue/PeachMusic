package com.example.peachmusic.domain.artist.controller;

import com.example.peachmusic.common.model.*;
import com.example.peachmusic.domain.artist.dto.response.ArtistGetDetailResponseDto;
import com.example.peachmusic.domain.artist.dto.response.ArtistPreviewResponseDto;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import com.example.peachmusic.domain.artist.service.ArtistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<CommonResponse<ArtistGetDetailResponseDto>> getArtistDetail(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("artistId") Long artistId
    ) {
       ArtistGetDetailResponseDto responseDto = artistService.getArtistDetail(authUser, artistId);

       return ResponseEntity.ok(CommonResponse.success("아티스트 조회에 성공했습니다.", responseDto));
    }

    /**
     * 아티스트의 앨범 및 음원 미리보기
     */
    @GetMapping("/artists/{artistId}/preview")
    public ResponseEntity<CommonResponse<ArtistPreviewResponseDto>> getArtistDetailPreview(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("artistId") Long artistId
    ) {
        ArtistPreviewResponseDto responseDto = artistService.getArtistDetailPreview(authUser, artistId);

        return ResponseEntity.ok(CommonResponse.success("아티스트의 앨범 및 음원 미리보기가 성공했습니다.", responseDto));
    }

    /**
     * 아티스트 검색
     * - Keyset 페이징 적용
     * - 좋아요 많은 순이 기본 정렬
     * @param condition 검색어, 정렬 기준, 정렬 방향, 커서
     * @return 아티스트 검색 결과, 다음 데이터 있는지 여부, 커서
     */
    @GetMapping("/search/artists")
    public ResponseEntity<CommonResponse<KeysetResponse<ArtistSearchResponseDto>>> searchArtist(
            @Valid @ModelAttribute SearchConditionParam condition,
            @ModelAttribute CursorParam cursor
    ) {
        KeysetResponse<ArtistSearchResponseDto> responseDto = artistService.searchArtistPage(condition, cursor);

        return ResponseEntity.ok(CommonResponse.success("아티스트 검색이 완료되었습니다.", responseDto));
    }
}
