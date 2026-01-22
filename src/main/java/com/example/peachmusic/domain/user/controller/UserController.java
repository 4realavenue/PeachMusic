package com.example.peachmusic.domain.user.controller;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.user.dto.request.LoginRequestDto;
import com.example.peachmusic.domain.user.dto.request.UserCreateRequestDto;
import com.example.peachmusic.domain.user.dto.request.UserUpdateRequestDto;
import com.example.peachmusic.domain.user.dto.response.UserCreateResponseDto;
import com.example.peachmusic.domain.user.dto.response.UserGetResponseDto;
import com.example.peachmusic.domain.user.dto.response.LoginResponseDto;
import com.example.peachmusic.domain.user.dto.response.admin.UserUpdateResponseDto;
import com.example.peachmusic.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.aspectj.ConfigurableObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final ConfigurableObject configurableObject;

    @PostMapping("/auth/signup")
    public ResponseEntity<CommonResponse<UserCreateResponseDto>> createUser(
            @Valid @RequestBody UserCreateRequestDto request
    ) {
        UserCreateResponseDto response = userService.createUser(request);

        return new ResponseEntity<>(CommonResponse.success("유저 생성 성공", response), HttpStatus.CREATED);
    }


    @GetMapping("/users")
    public ResponseEntity<CommonResponse<UserGetResponseDto>>  getUser(
            @AuthenticationPrincipal AuthUser authUser
            ) {

        UserGetResponseDto response = userService.getUser(authUser);

        return ResponseEntity.ok(CommonResponse.success("유저 조회 성공", response));

    }

    @PutMapping("/users")
    public ResponseEntity<CommonResponse<UserUpdateResponseDto>> updateUser(
            @Valid @RequestBody UserUpdateRequestDto request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        UserUpdateResponseDto result = userService.update(request, authUser);

        return ResponseEntity.ok(CommonResponse.success("유저 정보 수정 성공",result));
    }

    @DeleteMapping("/users")
    public ResponseEntity deleteUser(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        userService.deleteUser(authUser);

        return ResponseEntity.ok(CommonResponse.success("유저 비활성화 성공"));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<CommonResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        LoginResponseDto responseDto = userService.login(request);

        return ResponseEntity.ok(CommonResponse.success("로그인 성공",responseDto));
    }

    @DeleteMapping("/auth/logout")
    public ResponseEntity<CommonResponse<Void>> logout(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        userService.logout(authUser.getUserId());

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(CommonResponse.success("로그아웃 완료"));
    }


}
