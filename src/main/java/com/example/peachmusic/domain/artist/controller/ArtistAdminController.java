package com.example.peachmusic.domain.artist.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.common.model.PageResponse;
import com.example.peachmusic.domain.artist.dto.request.ArtistCreateRequestDto;
import com.example.peachmusic.domain.artist.dto.request.ArtistUpdateRequestDto;
import com.example.peachmusic.domain.artist.dto.response.ArtistCreateResponseDto;
import com.example.peachmusic.domain.artist.dto.response.ArtistImageUpdateResponseDto;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import com.example.peachmusic.domain.artist.dto.response.ArtistUpdateResponseDto;
import com.example.peachmusic.domain.artist.service.ArtistAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ArtistAdminController {

    private final ArtistAdminService artistAdminService;

    /**
     * 아티스트 생성 API (관리자 전용)
     *
     * @param requestDto 아티스트 생성 요청 DTO
     * @param profileImage 업로드할 아티스트 프로필 이미지 파일 (선택)
     * @return 생성된 아티스트 정보
     */
    @PostMapping("/admin/artists")
    public ResponseEntity<CommonResponse<ArtistCreateResponseDto>> createArtist(
            @RequestPart("request") @Valid ArtistCreateRequestDto requestDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        ArtistCreateResponseDto responseDto = artistAdminService.createArtist(requestDto, profileImage);

        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success("아티스트가 생성되었습니다.", responseDto));
    }

    /**
     * 전체 아티스트 조회 API (관리자 전용)
     *
     * @param pageable pageable 페이지네이션 및 정렬 정보 (기본 정렬: 아티스트 ID 오름차순)
     * @return 아티스트 목록 페이징 조회 결과
     */
    @GetMapping("/admin/artists")
    public ResponseEntity<PageResponse<ArtistSearchResponseDto>> getArtistList(
            @RequestParam(required = false) String word,
            @PageableDefault(page = 0, size = 10, sort = "artistId", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<ArtistSearchResponseDto> responseDtoPage = artistAdminService.getArtistList(word, pageable);

        return ResponseEntity.ok(PageResponse.success("아티스트 목록 조회에 성공했습니다.", responseDtoPage));
    }

    /**
     * 아티스트 기본 정보 수정 API (관리자 전용)
     *
     * @param artistId 수정할 아티스트 ID
     * @param requestDto 아티스트 기본 정보 수정 요청 DTO
     * @return 수정된 아티스트 정보
     */
    @PutMapping("/admin/artists/{artistId}")
    public ResponseEntity<CommonResponse<ArtistUpdateResponseDto>> updateArtist(
            @PathVariable("artistId") Long artistId,
            @Valid @RequestBody ArtistUpdateRequestDto requestDto) {

        ArtistUpdateResponseDto responseDto = artistAdminService.updateArtist(artistId, requestDto);

        return ResponseEntity.ok(CommonResponse.success("아티스트 정보가 수정되었습니다.", responseDto));
    }

    /**
     * 아티스트 프로필 이미지 수정 API (관리자 전용)
     *
     * @param artistId 프로필 이미지를 수정할 아티스트 ID
     * @param profileImage 업로드할 새로운 프로필 이미지 파일
     * @return 수정된 프로필 이미지가 반영된 아티스트 정보
     */
    @PatchMapping("/admin/artists/{artistId}/profile-image")
    public ResponseEntity<CommonResponse<ArtistImageUpdateResponseDto>> updateProfileImage(
            @PathVariable("artistId") Long artistId,
            @RequestParam MultipartFile profileImage) {

        ArtistImageUpdateResponseDto responseDto = artistAdminService.updateProfileImage(artistId, profileImage);

        return ResponseEntity.ok(CommonResponse.success("아티스트 프로필 이미지가 수정되었습니다.", responseDto));
    }

    /**
     * 아티스트 비활성화 API (관리자 전용)
     *
     * @param artistId 비활성화할 아티스트 ID
     * @return 응답 데이터 없이 성공 메시지만 반환
     */
    @DeleteMapping("/admin/artists/{artistId}")
    public ResponseEntity<CommonResponse<Void>> deleteArtist(
            @PathVariable("artistId") Long artistId) {

        artistAdminService.deleteArtist(artistId);

        return ResponseEntity.ok(CommonResponse.success("아티스트가 비활성화 되었습니다."));
    }

    /**
     * 아티스트 활성화 API (관리자 전용)
     *
     * @param artistId 활성화할 아티스트 ID
     * @return 응답 데이터 없이 성공 메시지만 반환
     */
    @PatchMapping("/admin/artists/{artistId}/restore")
    public ResponseEntity<CommonResponse<Void>> restoreArtist(
            @PathVariable("artistId") Long artistId) {

        artistAdminService.restoreArtist(artistId);

        return ResponseEntity.ok(CommonResponse.success("아티스트가 활성화 되었습니다."));
    }
}
