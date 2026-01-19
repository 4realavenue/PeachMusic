package com.example.peachmusic.domain.user.service;

import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
import com.example.peachmusic.common.model.PageResponse;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.model.UserDto;
import com.example.peachmusic.domain.user.model.response.admin.UserAdminGetResponse;
import com.example.peachmusic.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAdminService {


    private final UserRepository userRepository;
    private final UserService userService;

    // 전체 조회
    public PageResponse<UserAdminGetResponse> getAllUser(Pageable pageable) {

        Page<User> users = userRepository.findAllUser(pageable);

        Page<UserAdminGetResponse> response = users.map(user ->
                UserAdminGetResponse.from(UserDto.from(user))
        );

        return PageResponse.success("전체 유저 조회 성공", response);
    }


    // 유저 삭제
    public void deleteUser(Long userId) {
        User findUser = userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        findUser.delete();


    }

    // 유저 복구
    public void restorationUser(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!user.isDeleted()) {
            throw new IllegalStateException("이미 활성화된 사용자입니다.");
        }

        user.restore();

    }

    // UserAdminService.java - 유저 권한 변경 (ADMIN ↔ USER)
    public void role(Long userId, UserRole newRole) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 이미 같은 권한이면 변경 불필요
        if (user.getRole() == newRole) {
            throw new IllegalStateException("이미 해당 권한입니다.");
        }

        // 권한 변경
        user.setRole(newRole);

    }
}
