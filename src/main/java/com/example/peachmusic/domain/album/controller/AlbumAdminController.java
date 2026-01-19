package com.example.peachmusic.domain.album.controller;

import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.common.model.PageResponse;
import com.example.peachmusic.domain.album.model.request.AlbumCreateRequestDto;
import com.example.peachmusic.domain.album.model.request.AlbumUpdateRequestDto;
import com.example.peachmusic.domain.album.model.request.ArtistAlbumUpdateRequestDto;
import com.example.peachmusic.domain.album.model.response.AlbumCreateResponseDto;
import com.example.peachmusic.domain.album.model.response.AlbumGetAllResponseDto;
import com.example.peachmusic.domain.album.model.response.AlbumUpdateResponseDto;
import com.example.peachmusic.domain.album.model.response.ArtistAlbumUpdateResponseDto;
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
     * JWT 적용 전 단계로, 요청 헤더에서 사용자 식별 정보와 권한을 임시로 전달받는다.
     *
     * @param userId 요청 헤더에 전달되는 사용자 ID (JWT 적용 전까지 임시 사용)
     * @param role 요청 헤더에 전달되는 사용자 권한 (X-ROLE은 ADMIN 대문자)
     * @param requestDto 앨범 생성 요청 DTO
     * @return 생성된 앨범 정보
     */
    @PostMapping("/admin/albums")
    public ResponseEntity<CommonResponse<AlbumCreateResponseDto>> createAlbum(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-ROLE") UserRole role,
            @Valid @RequestBody AlbumCreateRequestDto requestDto) {

        AlbumCreateResponseDto responseDto = albumAdminService.createAlbum(userId, role, requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success("앨범 생성 성공", responseDto));
    }

    /**
     * 전체 앨범 조회 API (관리자 전용)
     * JWT 적용 전 단계로, 요청 헤더에서 사용자 식별 정보와 권한을 임시로 전달받는다.
     *
     * @param userId 요청 헤더에 전달되는 사용자 ID (JWT 적용 전까지 임시 사용)
     * @param role 요청 헤더에 전달되는 사용자 권한 (X-ROLE은 ADMIN 대문자)
     * @param pageable pageable 페이지네이션 및 정렬 정보 (기본 정렬: 앨범 발매일 내림차순)
     * @return 앨범 목록 페이징 조회 결과
     */
    @GetMapping("/admin/albums")
    public ResponseEntity<PageResponse<AlbumGetAllResponseDto>> getAlbumList(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-ROLE") UserRole role,
            @PageableDefault(page = 0, size = 10, sort = "albumReleaseDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AlbumGetAllResponseDto> responseDtoPage = albumAdminService.getAlbumList(userId, role, pageable);

        return ResponseEntity.ok(PageResponse.success("앨범 목록 조회 성공", responseDtoPage));
    }

    /**
     * 앨범 기본 정보 수정 API (관리자 전용)
     * JWT 적용 전 단계로, 요청 헤더에서 사용자 식별 정보와 권한을 임시로 전달받는다.
     *
     * @param userId 요청 헤더에 전달되는 사용자 ID (JWT 적용 전까지 임시 사용)
     * @param role 요청 헤더에 전달되는 사용자 권한 (X-ROLE은 ADMIN 대문자)
     * @param albumId 수정할 앨범 ID
     * @param requestDto 앨범 수정 요청 DTO
     * @return 수정된 앨범 정보
     */
    @PatchMapping("/admin/albums/{albumId}")
    public ResponseEntity<CommonResponse<AlbumUpdateResponseDto>> updateAlbum(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-ROLE") UserRole role,
            @PathVariable("albumId") Long albumId,
            @RequestBody AlbumUpdateRequestDto requestDto) {

        AlbumUpdateResponseDto responseDto = albumAdminService.updateAlbumInfo(userId, role, albumId, requestDto);

        return ResponseEntity.ok(CommonResponse.success("앨범 기본 정보 수정 완료", responseDto));
    }

    /**
     * 참여 아티스트 목록 전체 갱신 API (관리자 전용)
     * JWT 적용 전 단계로, 요청 헤더에서 사용자 식별 정보와 권한을 임시로 전달받는다.
     *
     * @param userId 요청 헤더에 전달되는 사용자 ID (JWT 적용 전까지 임시 사용)
     * @param role 요청 헤더에 전달되는 사용자 권한 (X-ROLE은 ADMIN 대문자)
     * @param albumId 갱신할 앨범 ID
     * @param requestDto 참여 아티스트 수정 요청 DTO
     * @return 참여 아티스트가 반영된 앨범 정보
     */
    @PutMapping("/admin/albums/{albumId}/artists")
    public ResponseEntity<CommonResponse<ArtistAlbumUpdateResponseDto>> updateAlbumArtistList(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-ROLE") UserRole role,
            @PathVariable("albumId") Long albumId,
            @Valid @RequestBody ArtistAlbumUpdateRequestDto requestDto) {

        ArtistAlbumUpdateResponseDto responseDto = albumAdminService.updateAlbumArtistList(userId, role, albumId, requestDto);

        return ResponseEntity.ok(CommonResponse.success("참여 아티스트 목록 갱신 완료", responseDto));
    }
}
