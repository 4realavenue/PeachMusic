package com.example.peachmusic.domain.artist.controller;

import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.common.model.PageResponse;
import com.example.peachmusic.domain.artist.model.request.ArtistCreateRequestDto;
import com.example.peachmusic.domain.artist.model.response.ArtistCreateResponseDto;
import com.example.peachmusic.domain.artist.model.response.ArtistGetAllResponseDto;
import com.example.peachmusic.domain.artist.service.ArtistAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/artists")
public class ArtistAdminController {

    private final ArtistAdminService artistAdminService;

    /**
     * 아티스트 생성 API (관리자 전용)
     * JWT 적용 전 단계로, 요청 헤더에서 사용자 식별 정보와 권한을 임시로 전달받는다.
     *
     * @param userId 요청 헤더에 전달되는 사용자 ID (JWT 적용 전까지 임시 사용)
     * @param role 요청 헤더에 전달되는 사용자 권한 (X-ROLE은 ADMIN 대문자)
     * @param requestDto 아티스트 생성 요청 DTO
     * @return 생성된 아티스트 정보
     */
    @PostMapping
    public ResponseEntity<CommonResponse<ArtistCreateResponseDto>> createArtist(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-ROLE") UserRole role,
            @Valid @RequestBody ArtistCreateRequestDto requestDto) {

        ArtistCreateResponseDto responseDto = artistAdminService.createArtist(userId, role, requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success("아티스트 생성 성공", responseDto));
    }

    /**
     * 전체 아티스트 조회 API (관리자 전용)
     * JWT 적용 전 단계로, 요청 헤더에서 사용자 식별 정보와 권한을 임시로 전달받는다.
     *
     * @param userId 요청 헤더에 전달되는 사용자 ID (JWT 적용 전까지 임시 사용)
     * @param role 요청 헤더에 전달되는 사용자 권한 (X-ROLE은 ADMIN 대문자)
     * @param pageable pageable 페이지네이션 및 정렬 정보 (기본 정렬: 생성 시점 내림차순)
     * @return 아티스트 목록 페이징 조회 결과
     */
    @GetMapping
    public ResponseEntity<PageResponse<ArtistGetAllResponseDto>> getArtistList(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-ROLE") UserRole role,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponse<ArtistGetAllResponseDto> response = artistAdminService.getArtistList(userId, role, pageable);

        return ResponseEntity.ok(response);
    }
}
