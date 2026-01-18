package com.example.peachmusic.domain.user.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.model.UserDto;
import com.example.peachmusic.domain.user.model.request.UserCreateRequestDto;
import com.example.peachmusic.domain.user.model.response.UserCreateResponseDto;
import com.example.peachmusic.domain.user.model.response.UserGetResponseDto;
import com.example.peachmusic.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    // 생성
    public UserCreateResponseDto createUser(@Valid UserCreateRequestDto request) {

        User user = new User(
                request.getName(),
                request.getNickname(),
                request.getEmail(),
                request.getPassword()
        );

        userRepository.save(user);

        UserDto dto = UserDto.from(user);

        return UserCreateResponseDto.from(dto);
    }

    // 내 정보 조회 jwt 후 헤더의 ID로 조회로직 작성 예정
    public UserGetResponseDto getUser(Long userId) {  // ← userId를 파라미터로 받음

        User user = userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        UserDto dto = UserDto.from(user);

        return UserGetResponseDto.from(dto);
    }

    // 유저 삭제
    public void deleteUser(Long userId) {
        User findUser = userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        findUser.delete();


    }

}
