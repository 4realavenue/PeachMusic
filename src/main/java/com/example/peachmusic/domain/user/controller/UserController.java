package com.example.peachmusic.domain.user.controller;

import com.example.peachmusic.common.model.AuthUser;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

        return new ResponseEntity<>(CommonResponse.success("유저 생성 성공", response), HttpStatus.CREATED);
    }


    // 단일 조회
    @GetMapping("/users")
    public ResponseEntity<CommonResponse<UserGetResponseDto>>  getUser(
            @AuthenticationPrincipal AuthUser authUser
            ) {

        UserGetResponseDto response = userService.getUser(authUser);

        return ResponseEntity.ok(CommonResponse.success("유저 조회 성공", response));

    }

    // 정보 수정
    @PutMapping("/users")
    public ResponseEntity<CommonResponse<UserUpdateResponseDto>> updateUser(
            @Valid @RequestBody UserUpdateRequestDto request,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        UserUpdateResponseDto result = userService.update(request, authUser);




        return ResponseEntity.ok(CommonResponse.success("유저 정보 수정 성공",result));
    }

    // 삭제
    @DeleteMapping("/users")
    public ResponseEntity deleteUser(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        userService.deleteUser(authUser);

        return ResponseEntity.ok(CommonResponse.success("유저 비활성화 성공"));
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
