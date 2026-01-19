package com.example.peachmusic.domain.user.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.user.model.request.LoginRequestDto;
import com.example.peachmusic.domain.user.model.request.UserCreateRequestDto;
import com.example.peachmusic.domain.user.model.request.UserUpdateRequestDto;
import com.example.peachmusic.domain.user.model.response.UserCreateResponseDto;
import com.example.peachmusic.domain.user.model.response.UserGetResponseDto;
import com.example.peachmusic.domain.user.model.response.LoginResponseDto;
import com.example.peachmusic.domain.user.model.response.admin.UserUpdateResponseDto;
import com.example.peachmusic.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.aspectj.ConfigurableObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final ConfigurableObject configurableObject;

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

    // 정보 수정
    @PutMapping("/users/{userId}")
    public ResponseEntity<CommonResponse<UserUpdateResponseDto>> updateUser(
            @Valid @RequestBody UserUpdateRequestDto request,
            @PathVariable Long userId
    ) {

        UserUpdateResponseDto result = userService.update(request, userId);

        CommonResponse<UserUpdateResponseDto> commonResponse = new CommonResponse<>(true, "유저 정보 수정 성공",result);

        ResponseEntity<CommonResponse<UserUpdateResponseDto>> response = new ResponseEntity<>(commonResponse, HttpStatus.OK);

        return response;
    }

    // 삭제
    @DeleteMapping("/users/{userId}")
    public ResponseEntity deleteUser(
            @PathVariable Long userId
    ) {
        userService.deleteUser(userId);

        CommonResponse response = new CommonResponse<>(true, "유저 비활성화 성공", null);

        return ResponseEntity.ok(response);
    }

    // 로그인
    @PostMapping("/auth/login")
    public ResponseEntity<CommonResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        String token = userService.login(request);

        LoginResponseDto responseDto = new LoginResponseDto(token);

        CommonResponse<LoginResponseDto> commonResponse = new CommonResponse<>(true, "로그인 성공", responseDto);

        ResponseEntity<CommonResponse<LoginResponseDto>> response = new ResponseEntity<>(commonResponse, HttpStatus.OK);

        return response;
    }


}
