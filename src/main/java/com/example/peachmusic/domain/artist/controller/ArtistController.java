package com.example.peachmusic.domain.artist.controller;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.domain.artist.dto.response.ArtistGetDetailResponseDto;
import com.example.peachmusic.domain.artist.service.ArtistService;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<CommonResponse<ArtistGetDetailResponseDto>> getArtistDetail(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("artistId") Long artistId
    ) {
       ArtistGetDetailResponseDto responseDto = artistService.getArtistDetail(authUser, artistId);

       return ResponseEntity.ok(CommonResponse.success("아티스트 조회에 성공했습니다.", responseDto));
    }

    /**
     * 아티스트 검색
     * - Keyset 페이징 적용
     * @param word 검색어
     * @param sortType 정렬 기준
     * @param direction 정렬 방향
     * @param lastId 커서 - 마지막 아티스트 ID
     * @param lastLike 커서 - 마지막 좋아요 수
     * @param lastName 커서 - 마지막 이름
     * @return 아티스트 검색 결과, 다음 데이터 있는지 여부, 커서
     */
    @GetMapping("/search/artists")
    public ResponseEntity<CommonResponse<KeysetResponse<ArtistSearchResponseDto>>> searchArtist(
            @RequestParam String word,
            @RequestParam(defaultValue = "LIKE") SortType sortType,
            @RequestParam(defaultValue = "DESC") SortDirection direction,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false) Long lastLike,
            @RequestParam(required = false) String lastName
    ) {
        KeysetResponse<ArtistSearchResponseDto> responseDto = artistService.searchArtistPage(word, sortType, direction, lastId, lastLike, lastName);

        return ResponseEntity.ok(CommonResponse.success("아티스트 검색이 완료되었습니다.", responseDto));
    }
}
