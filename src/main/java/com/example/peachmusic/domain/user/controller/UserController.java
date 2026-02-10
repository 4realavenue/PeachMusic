package com.example.peachmusic.domain.user.controller;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.domain.albumlike.dto.response.AlbumLikedItemResponseDto;
import com.example.peachmusic.domain.albumlike.service.AlbumLikeService;
import com.example.peachmusic.domain.artistlike.dto.response.ArtistLikedItemResponseDto;
import com.example.peachmusic.domain.artistlike.service.ArtistLikeService;
import com.example.peachmusic.domain.songlike.dto.response.SongLikedItemResponseDto;
import com.example.peachmusic.domain.songlike.service.SongLikeService;
import com.example.peachmusic.domain.user.dto.request.UserUpdateRequestDto;
import com.example.peachmusic.domain.user.dto.response.UserGetResponseDto;
import com.example.peachmusic.domain.user.dto.response.admin.UserUpdateResponseDto;
import com.example.peachmusic.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final AlbumLikeService albumLikeService;
    private final ArtistLikeService artistLikeService;
    private final SongLikeService songLikeService;

    /**
     *  내 정보 조회
     */
    @GetMapping("/users")
    public ResponseEntity<CommonResponse<UserGetResponseDto>> getUser(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        UserGetResponseDto response = userService.getUser(authUser);
        return ResponseEntity.ok(CommonResponse.success("유저 조회 성공", response));
    }

    /**
     *  내 정보 수정
     */
    @PatchMapping("/users")
    public ResponseEntity<CommonResponse<UserUpdateResponseDto>> updateUser(
            @Valid @RequestBody UserUpdateRequestDto request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        UserUpdateResponseDto result = userService.update(request, authUser);
        return ResponseEntity.ok(CommonResponse.success("유저 정보 수정 성공했습니다.", result));
    }

    /**
     *  비활성화
     */
    @DeleteMapping("/users")
    public ResponseEntity<CommonResponse<Void>> deleteUser(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        userService.deleteUser(authUser);
        return ResponseEntity.ok(CommonResponse.success("유저 비활성화를 성공했습니다."));
    }

    /**
     * 내가 좋아요한 앨범 목록 조회
     *
     * @param lastLikeId 이전 페이지의 마지막 albumLikeId
     *                  (첫 페이지 조회 시 null)
     * @return 내가 좋아요한 앨범 목록과 다음 페이지 여부 및 커서를 포함한
     *         Keyset 기반 페이징 응답
     */
    @GetMapping("/users/likes/albums")
    public ResponseEntity<CommonResponse<KeysetResponse<AlbumLikedItemResponseDto>>> getMyLikedAlbum(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) Long lastLikeId
    ) {
        Long userId = authUser.getUserId();

        KeysetResponse<AlbumLikedItemResponseDto> responseDtoPage = albumLikeService.getMyLikedAlbum(userId, lastLikeId);
        return ResponseEntity.ok(CommonResponse.success("좋아요한 앨범 목록 조회에 성공했습니다.", responseDtoPage));
    }

    /**
     * 내가 좋아요한 아티스트 목록 조회
     *
     * @param lastLikeId 이전 페이지의 마지막 artistLikeId
     *                  (첫 페이지 조회 시 null)
     * @return 내가 좋아요한 아티스트 목록과 다음 페이지 여부 및 커서를 포함한
     *         Keyset 기반 페이징 응답
     */
    @GetMapping("/users/likes/artists")
    public ResponseEntity<CommonResponse<KeysetResponse<ArtistLikedItemResponseDto>>> getMyLikedArtist(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) Long lastLikeId
    ) {
        Long userId = authUser.getUserId();

        KeysetResponse<ArtistLikedItemResponseDto> responseDtoPage = artistLikeService.getMyLikedArtist(userId, lastLikeId);
        return ResponseEntity.ok(CommonResponse.success("좋아요한 아티스트 목록 조회에 성공했습니다.", responseDtoPage));
    }

    /**
     * 내가 좋아요한 음원 목록 조회
     *
     * @param lastLikeId 이전 페이지의 마지막 songLikeId
     *                  (첫 페이지 조회 시 null)
     * @return 내가 좋아요한 음원 목록과 다음 페이지 여부 및 커서를 포함한
     *         Keyset 기반 페이징 응답
     */
    @GetMapping("/users/likes/songs")
    public ResponseEntity<CommonResponse<KeysetResponse<SongLikedItemResponseDto>>> getMyLikedSong(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) Long lastLikeId
    ) {
        Long userId = authUser.getUserId();

        KeysetResponse<SongLikedItemResponseDto> responseDtoPage = songLikeService.getMyLikedSong(userId, lastLikeId);
        return ResponseEntity.ok(CommonResponse.success("좋아요한 음원 목록 조회에 성공했습니다.", responseDtoPage));
    }
}