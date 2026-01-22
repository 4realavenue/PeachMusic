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

import static org.apache.logging.log4j.util.Strings.isNotBlank;

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

//      1. 기존 유저 조회
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

//      2. 복사본 생성
        User updated = user.toBuilder()
                .name(isNotBlank(request.getName()) ? request.getName().trim() : user.getName())
                .nickname(isNotBlank(request.getNickname()) ? request.getNickname().trim() : user.getNickname())
                .build();

//      3. 닉네임이 변경되었을 때, 중복 체크
        if (isNotBlank(request.getNickname()) && !request.getNickname().trim().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new CustomException(ErrorCode.USER_EXIST_NICKNAME);
            }
        }

        userRepository.save(updated);

        return UserUpdateResponseDto.from(updated);
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
