package com.example.peachmusic.domain.album.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.album.model.response.AlbumGetDetailResponseDto;
import com.example.peachmusic.domain.album.service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<CommonResponse<AlbumGetDetailResponseDto>> getAlbumDetail(@PathVariable("albumId") Long albumId) {

        AlbumGetDetailResponseDto responseDto = albumService.getAlbumDetail(albumId);

        return ResponseEntity.ok(CommonResponse.success("앨범 조회 성공", responseDto));
    }
}
