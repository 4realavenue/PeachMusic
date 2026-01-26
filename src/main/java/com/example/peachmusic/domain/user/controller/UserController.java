package com.example.peachmusic.domain.user.controller;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.user.dto.request.LoginRequestDto;
import com.example.peachmusic.domain.user.dto.request.UserCreateRequestDto;
import com.example.peachmusic.domain.user.dto.request.UserUpdateRequestDto;
import com.example.peachmusic.domain.user.dto.response.LoginResponseDto;
import com.example.peachmusic.domain.user.dto.response.UserCreateResponseDto;
import com.example.peachmusic.domain.user.dto.response.UserGetResponseDto;
import com.example.peachmusic.domain.user.dto.response.admin.UserUpdateResponseDto;
import com.example.peachmusic.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;



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


}