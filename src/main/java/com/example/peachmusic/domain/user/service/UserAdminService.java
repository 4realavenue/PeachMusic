package com.example.peachmusic.domain.user.service;

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

import static com.example.peachmusic.domain.user.entity.QUser.user;

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


}
