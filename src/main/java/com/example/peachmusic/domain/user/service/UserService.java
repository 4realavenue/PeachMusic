package com.example.peachmusic.domain.user.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.user.dto.request.UserUpdateRequestDto;
import com.example.peachmusic.domain.user.dto.response.UserGetResponseDto;
import com.example.peachmusic.domain.user.dto.response.admin.UserUpdateResponseDto;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 내 정보 조회
     */
    @Transactional(readOnly = true)
    public UserGetResponseDto getUser(AuthUser authUser) {
        return UserGetResponseDto.from(findUser(authUser));
    }

    /**
     * 내 정보 수정
     */
    @Transactional
    public UserUpdateResponseDto update(UserUpdateRequestDto request, AuthUser authUser) {

        User user = findUser(authUser);

        if (isNotBlank(request.getNickname()) && !request.getNickname().trim().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname().trim())) {
                throw new CustomException(ErrorCode.USER_EXIST_NICKNAME);
            }
        }

        user.update(request);
        return UserUpdateResponseDto.from(user);
    }

    /**
     *  비활성화
     */
    @Transactional
    public void deleteUser(AuthUser authUser) {
        findUser(authUser).delete();
    }

    /**
     *  로그아웃
     */
    @Transactional
    public void logout(AuthUser authUser) {
        findUser(authUser).increaseTokenVersion();
    }

    /**
     * DB에서 유저 조회
     */
    public User findUser(AuthUser authUser) {
        return userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}