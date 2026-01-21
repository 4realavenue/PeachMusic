package com.example.peachmusic.domain.user.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.filter.JwtUtil;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.dto.request.LoginRequestDto;
import com.example.peachmusic.domain.user.dto.request.UserCreateRequestDto;
import com.example.peachmusic.domain.user.dto.request.UserUpdateRequestDto;
import com.example.peachmusic.domain.user.dto.response.LoginResponseDto;
import com.example.peachmusic.domain.user.dto.response.UserCreateResponseDto;
import com.example.peachmusic.domain.user.dto.response.UserGetResponseDto;
import com.example.peachmusic.domain.user.dto.response.admin.UserUpdateResponseDto;
import com.example.peachmusic.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

//        1. 유저정보 담기
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

//        2. 닉네임 변경
        if (request.getNickname() != null) {
            String newNickname = request.getNickname().trim();

            // 변경할 닉네임이 기존의 닉네임과 다를경우 중복 확인
            if (!newNickname.equals(user.getNickname())) {
                if (userRepository.existsByNickname(newNickname)) {
                    throw new CustomException(ErrorCode.USER_EXIST_NICKNAME);
                }
                user.setNickname(newNickname);
            }
        }

        // 2. 이름 변경
        if (request.getName() != null) {
            user.setName(request.getName().trim());
        }

        // 3.둘다 null 이면 예외처리
        if (request.getName() == null && request.getNickname() == null) {
            throw new CustomException(ErrorCode.AUTH_NAME_NICKNAME_REQUIRED);
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

        AuthUser authUser = new AuthUser(user.getUserId(), user.getEmail(), user.getRole(), user.getTokenVersion());

        Authentication authentication = new UsernamePasswordAuthenticationToken(authUser,null, authUser.getAuthoritie());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return new LoginResponseDto(token);
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.increaseTokenVersion();
    }

}
