package com.example.peachmusic.domain.user.service;

import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.model.UserDto;
import com.example.peachmusic.domain.user.model.request.UserCreateRequestDto;
import com.example.peachmusic.domain.user.model.response.UserCreateResponseDto;
import com.example.peachmusic.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

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




}
