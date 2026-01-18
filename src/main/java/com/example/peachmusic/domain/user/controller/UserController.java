package com.example.peachmusic.domain.user.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.user.model.request.UserCreateRequestDto;
import com.example.peachmusic.domain.user.model.response.UserCreateResponseDto;
import com.example.peachmusic.domain.user.model.response.UserGetResponseDto;
import com.example.peachmusic.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    // 생성
    @PostMapping("/auth/signup")
    public ResponseEntity<CommonResponse<UserCreateResponseDto>> createUser(
            @Valid @RequestBody UserCreateRequestDto request
    ) {
        UserCreateResponseDto response = userService.createUser(request);

        CommonResponse<UserCreateResponseDto> commonResponse = new CommonResponse<>(true, "유저 생성 성공", response);

        return new ResponseEntity<>(commonResponse, HttpStatus.CREATED);
    }


    // 단일 조회
    // 내 정보 조회 jwt 후 헤더의 ID로 조회로직 작성 예정
    @GetMapping("/users/{userId}")
    public ResponseEntity<CommonResponse<UserGetResponseDto>>  getUser(
            @PathVariable Long userId
    ) {

        UserGetResponseDto response = userService.getUser(userId);

        CommonResponse<UserGetResponseDto> commonResponse = new CommonResponse<>(true, "유저 조회 성공", response);
        return ResponseEntity.ok(commonResponse);

    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity deleteUser(
            @PathVariable Long userId
    ) {
        userService.deleteUser(userId);

        CommonResponse response = new CommonResponse<>(true, "유저 비활성화 성공", null);

        return ResponseEntity.ok(response);
    }
}
