package com.example.peachmusic.domain.artist.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.artist.model.response.ArtistGetDetailResponseDto;
import com.example.peachmusic.domain.artist.service.ArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<CommonResponse<ArtistGetDetailResponseDto>> getArtistDetail(@PathVariable("artistId") Long artistId) {

       ArtistGetDetailResponseDto responseDto = artistService.getArtistDetail(artistId);

       return ResponseEntity.ok(CommonResponse.success("아티스트 조회 성공", responseDto));
    }
}
