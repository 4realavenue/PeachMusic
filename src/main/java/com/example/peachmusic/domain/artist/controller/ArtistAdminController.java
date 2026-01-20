package com.example.peachmusic.domain.artist.controller;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.common.model.PageResponse;
import com.example.peachmusic.domain.artist.model.request.ArtistCreateRequestDto;
import com.example.peachmusic.domain.artist.model.request.ArtistUpdateRequestDto;
import com.example.peachmusic.domain.artist.model.response.ArtistCreateResponseDto;
import com.example.peachmusic.domain.artist.model.response.ArtistGetAllResponseDto;
import com.example.peachmusic.domain.artist.model.response.ArtistUpdateResponseDto;
import com.example.peachmusic.domain.artist.service.ArtistAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ArtistAdminController {

    private final ArtistAdminService artistAdminService;

    /**
     * 아티스트 생성 API (관리자 전용)
     *
     * @param authUser 인증된 사용자 정보
     * @param requestDto 아티스트 생성 요청 DTO
     * @return 생성된 아티스트 정보
     */
    @PostMapping("/admin/artists")
    public ResponseEntity<CommonResponse<ArtistCreateResponseDto>> createArtist(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ArtistCreateRequestDto requestDto) {

        ArtistCreateResponseDto responseDto = artistAdminService.createArtist(authUser, requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success("아티스트 생성 성공", responseDto));
    }

    /**
     * 전체 아티스트 조회 API (관리자 전용)
     *
     * @param authUser 인증된 사용자 정보
     * @param pageable pageable 페이지네이션 및 정렬 정보 (기본 정렬: 생성 시점 내림차순)
     * @return 아티스트 목록 페이징 조회 결과
     */
    @GetMapping("/admin/artists")
    public ResponseEntity<PageResponse<ArtistGetAllResponseDto>> getArtistList(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ArtistGetAllResponseDto> responseDtoPage = artistAdminService.getArtistList(authUser, pageable);

        return ResponseEntity.ok(PageResponse.success("아티스트 목록 조회 성공", responseDtoPage));
    }

    /**
     * 아티스트 수정 API (관리자 전용)
     *
     * @param authUser 인증된 사용자 정보
     * @param artistId 수정할 아티스트 ID
     * @param requestDto 아티스트 수정 요청 DTO
     * @return 수정된 아티스트 정보
     */
    @PutMapping("/admin/artists/{artistId}")
    public ResponseEntity<CommonResponse<ArtistUpdateResponseDto>> updateArtist(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("artistId") Long artistId,
            @Valid @RequestBody ArtistUpdateRequestDto requestDto) {

        ArtistUpdateResponseDto responseDto = artistAdminService.updateArtist(authUser, artistId, requestDto);

        return ResponseEntity.ok(CommonResponse.success("아티스트 정보 수정 성공", responseDto));
    }

    /**
     * 아티스트 비활성화 API (관리자 전용)
     *
     * @param authUser 인증된 사용자 정보
     * @param artistId 비활성화할 아티스트 ID
     * @return 응답 데이터 없이 성공 메시지만 반환
     */
    @DeleteMapping("/admin/artists/{artistId}")
    public ResponseEntity<CommonResponse<Void>> deleteArtist(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("artistId") Long artistId) {

        artistAdminService.deleteArtist(authUser, artistId);

        return ResponseEntity.ok(CommonResponse.success("아티스트 비활성화 성공", null));
    }

    /**
     * 아티스트 활성화 API (관리자 전용)
     *
     * @param authUser 인증된 사용자 정보
     * @param artistId 활성화할 아티스트 ID
     * @return 응답 데이터 없이 성공 메시지만 반환
     */
    @PatchMapping("/admin/artists/{artistId}/restore")
    public ResponseEntity<CommonResponse<Void>> restoreArtist(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("artistId") Long artistId) {

        artistAdminService.restoreArtist(authUser, artistId);

        return ResponseEntity.ok(CommonResponse.success("아티스트 활성화 성공", null));
    }
}
