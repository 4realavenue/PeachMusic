package com.example.peachmusic.domain.album.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.common.model.PageResponse;
import com.example.peachmusic.domain.album.model.request.AlbumCreateRequestDto;
import com.example.peachmusic.domain.album.model.request.AlbumUpdateRequestDto;
import com.example.peachmusic.domain.album.model.request.ArtistAlbumUpdateRequestDto;
import com.example.peachmusic.domain.album.model.response.*;
import com.example.peachmusic.domain.album.service.AlbumAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AlbumAdminController {

    private final AlbumAdminService albumAdminService;

    /**
     * 앨범 생성 API (관리자 전용)
     *
     * @param requestDto 앨범 생성 요청 DTO
     * @return 생성된 앨범 정보
     */
    @PostMapping("/admin/albums")
    public ResponseEntity<CommonResponse<AlbumCreateResponseDto>> createAlbum(
            @Valid @RequestBody AlbumCreateRequestDto requestDto) {

        AlbumCreateResponseDto responseDto = albumAdminService.createAlbum(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success("앨범 생성 성공", responseDto));
    }

    /**
     * 전체 앨범 조회 API (관리자 전용)
     *
     * @param pageable pageable 페이지네이션 및 정렬 정보 (기본 정렬: 앨범 발매일 내림차순)
     * @return 앨범 목록 페이징 조회 결과
     */
    @GetMapping("/admin/albums")
    public ResponseEntity<PageResponse<AlbumSearchResponse>> getAlbumList(
            @RequestParam(required = false) String word,
            @PageableDefault(page = 0, size = 10, sort = "albumReleaseDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AlbumSearchResponse> responseDtoPage = albumAdminService.getAlbumList(word, pageable);

        return ResponseEntity.ok(PageResponse.success("앨범 목록 조회 성공", responseDtoPage));
    }

    /**
     * 앨범 기본 정보 수정 API (관리자 전용)
     *
     * @param albumId 수정할 앨범 ID
     * @param requestDto 앨범 수정 요청 DTO
     * @return 수정된 앨범 정보
     */
    @PatchMapping("/admin/albums/{albumId}")
    public ResponseEntity<CommonResponse<AlbumUpdateResponseDto>> updateAlbum(
            @PathVariable("albumId") Long albumId,
            @RequestBody AlbumUpdateRequestDto requestDto) {

        AlbumUpdateResponseDto responseDto = albumAdminService.updateAlbumInfo(albumId, requestDto);

        return ResponseEntity.ok(CommonResponse.success("앨범 기본 정보 수정 완료", responseDto));
    }

    /**
     * 참여 아티스트 목록 전체 갱신 API (관리자 전용)
     *
     * @param albumId 갱신할 앨범 ID
     * @param requestDto 참여 아티스트 수정 요청 DTO
     * @return 참여 아티스트가 반영된 앨범 정보
     */
    @PutMapping("/admin/albums/{albumId}/artists")
    public ResponseEntity<CommonResponse<ArtistAlbumUpdateResponseDto>> updateAlbumArtistList(
            @PathVariable("albumId") Long albumId,
            @Valid @RequestBody ArtistAlbumUpdateRequestDto requestDto) {

        ArtistAlbumUpdateResponseDto responseDto = albumAdminService.updateAlbumArtistList(albumId, requestDto);

        return ResponseEntity.ok(CommonResponse.success("참여 아티스트 목록 갱신 완료", responseDto));
    }

    /**
     * 앨범 비활성화 API (관리자 전용)
     *
     * @param albumId 비활성화할 앨범 ID
     * @return 응답 데이터 없이 성공 메시지만 반환
     */
    @DeleteMapping("/admin/albums/{albumId}")
    public ResponseEntity<CommonResponse<Void>> deleteAlbum(
            @PathVariable("albumId") Long albumId) {

        albumAdminService.deleteAlbum(albumId);

        return ResponseEntity.ok(CommonResponse.success("앨범 비활성화 성공", null));
    }

    /**
     * 앨범 활성화 API (관리자 전용)
     *
     * @param albumId 활성화할 앨범 ID
     * @return 응답 데이터 없이 성공 메시지만 반환
     */
    @PatchMapping("/admin/albums/{albumId}/restore")
    public ResponseEntity<CommonResponse<Void>> restoreAlbum(
            @PathVariable("albumId") Long albumId) {

        albumAdminService.restoreAlbum(albumId);

        return ResponseEntity.ok(CommonResponse.success("앨범 활성화 성공", null));
    }
}
