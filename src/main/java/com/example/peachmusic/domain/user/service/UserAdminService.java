package com.example.peachmusic.domain.user.service;

import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.model.response.admin.UserAdminGetResponseDto;
import com.example.peachmusic.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAdminService {


    private final UserRepository userRepository;

    // 전체 조회
    @Transactional(readOnly = true)
    public Page<UserAdminGetResponseDto> getAllUser(Pageable pageable) {

        Page<User> users = userRepository.findAll(pageable);

        Page<UserAdminGetResponseDto> response = users.map(user ->
                UserAdminGetResponseDto.from(user)
        );
        return response;
    }

    // 유저 삭제
    @Transactional
    public void deleteUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new CustomException(ErrorCode.USER_EXIST_DELETED);
        }
        user.delete();
    }

    // 유저 복구
    @Transactional
    public void restorationUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!user.isDeleted()) {
            throw new CustomException(ErrorCode.USER_EXIST_ACTIVATIONUSER);
        }
        user.restore();
    }

    // UserAdminService.java - 유저 권한 변경 (ADMIN ↔ USER)
    @Transactional
    public void role(Long userId, UserRole newRole) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        // 이미 같은 권한이면 변경 불필요
        if (user.getRole() == newRole) {
            throw new CustomException(ErrorCode.USER_EEXIST_ROLE);
        }
        // 권한 변경
        user.setRole(newRole);
    }
}
