package com.example.peachmusic.domain.user.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.filter.JwtUtil;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.model.request.LoginRequestDto;
import com.example.peachmusic.domain.user.model.request.UserCreateRequestDto;
import com.example.peachmusic.domain.user.model.request.UserUpdateRequestDto;
import com.example.peachmusic.domain.user.model.response.LoginResponseDto;
import com.example.peachmusic.domain.user.model.response.UserCreateResponseDto;
import com.example.peachmusic.domain.user.model.response.UserGetResponseDto;
import com.example.peachmusic.domain.user.model.response.admin.UserUpdateResponseDto;
import com.example.peachmusic.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 생성
    @Transactional
    public UserCreateResponseDto createUser(@Valid UserCreateRequestDto request) {

        String encodePassword = passwordEncoder.encode(request.getPassword());

        User user = new User(request.getName(), request.getNickname(), request.getEmail(), encodePassword);

        userRepository.save(user);

        return UserCreateResponseDto.from(user);
    }

    // 조회
    @Transactional(readOnly = true)
    public UserGetResponseDto getUser(AuthUser authUser) {  // ← userId를 파라미터로 받음

        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserGetResponseDto.from(user);
    }

    // 유저 수정
    @Transactional
    public UserUpdateResponseDto update(@Valid UserUpdateRequestDto request, AuthUser authUser) {
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 둘다 값이 안들어오면 에러
        if (request.getName() == null && request.getNickname() == null) {
            throw new  CustomException(ErrorCode.AUTH_NAME_NICKNAME_REQUIRED);
        }
        if (request.getName() != null ) { // 이름만 들어왔을때
            if (request.getNickname() != null) { // 이름과 닉네임 같이 들어왔을때
                user.setNickname(request.getNickname());
            }
             user.setName(request.getName());
        } if (request.getNickname() != null ) { // 닉네임만 들어왔을때
            user.setNickname(request.getNickname());
        }
        return UserUpdateResponseDto.from(user);
    }

    // 유저 삭제
    @Transactional
    public void deleteUser(AuthUser authUser) {

        User findUser = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        findUser.delete();
    }

    // 로그인
    @Transactional
    public LoginResponseDto login(@Valid LoginRequestDto request) {
        User user = userRepository.findUserByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.AUTH_INVALID_PASSWORD);
        }
        String token = jwtUtil.createToken(user.getUserId(), user.getEmail(), user.getRole(), user.getTokenVersion());

        return new LoginResponseDto(token);
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.increaseTokenVersion();
    }
}
