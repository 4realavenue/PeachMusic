package com.example.peachmusic.domain.user.controller;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.user.dto.request.LoginRequestDto;
import com.example.peachmusic.domain.user.dto.request.UserCreateRequestDto;
import com.example.peachmusic.domain.user.dto.response.LoginResponseDto;
import com.example.peachmusic.domain.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     *  회원가입
     */
    @PostMapping("/auth/signup")
    public ResponseEntity<CommonResponse<Void>> createUser(
            @Valid @RequestBody UserCreateRequestDto request
    ) {
        authService.createUser(request);
        return new ResponseEntity<>(CommonResponse.success("유저가 생성되었습니다."), HttpStatus.CREATED);
    }

    /**
     *  로그인
     */
    @PostMapping("/auth/login")
    public ResponseEntity<CommonResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        LoginResponseDto responseDto = authService.login(request);
        return ResponseEntity.ok(CommonResponse.success("로그인을 성공했습니다.", responseDto));
    }

    /**
     *  로그아웃
     */
    @DeleteMapping("/auth/logout")
    public ResponseEntity<CommonResponse<Void>> logout(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        authService.logout(authUser);
        return ResponseEntity.ok(CommonResponse.success("로그아웃을 완료했습니다."));
    }
}
