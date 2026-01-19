package com.example.peachmusic.domain.user.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
import com.example.peachmusic.common.filter.JwtUtil;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.model.UserDto;
import com.example.peachmusic.domain.user.model.request.LoginRequestDto;
import com.example.peachmusic.domain.user.model.request.UserCreateRequestDto;
import com.example.peachmusic.domain.user.model.request.UserUpdateRequestDto;
import com.example.peachmusic.domain.user.model.response.UserCreateResponseDto;
import com.example.peachmusic.domain.user.model.response.UserGetResponseDto;
import com.example.peachmusic.domain.user.model.response.admin.UserUpdateResponseDto;
import com.example.peachmusic.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 생성
    public UserCreateResponseDto createUser(@Valid UserCreateRequestDto request) {

        String encodePassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getName(),
                request.getNickname(),
                request.getEmail(),
                encodePassword
        );

        userRepository.save(user);


        return UserCreateResponseDto.from(user);
    }

    // 내 정보 조회 jwt 후 헤더의 ID로 조회로직 작성 예정
    public UserGetResponseDto getUser(Long userId) {  // ← userId를 파라미터로 받음

        User user = userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));


        return UserGetResponseDto.from(user);
    }

    // 유저 수정
    public UserUpdateResponseDto update(@Valid UserUpdateRequestDto request, Long userId) {
        User user = userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.UpdateUser(request.getName(), request.getNickname());

        return UserUpdateResponseDto.from(user);
    }

    // 유저 삭제
    public void deleteUser(Long userId) {
        User findUser = userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        findUser.delete();


    }

    // 로그인
    public String login(@Valid LoginRequestDto request) {

        String email = request.getEmail();
        String password = request.getPassword();

        User user = userRepository.findUserByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomException(ErrorCode.AUTH_INVALID_PASSWORD);
        }

        return jwtUtil.createToken(user.getUserId(),user.getEmail(),user.getRole());
    }
}
