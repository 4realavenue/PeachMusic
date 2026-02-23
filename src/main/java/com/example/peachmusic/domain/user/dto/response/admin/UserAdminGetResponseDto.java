package com.example.peachmusic.domain.user.dto.response.admin;

import com.example.peachmusic.common.enums.UserRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class UserAdminGetResponseDto {

    private final Long userId;
    private final UserRole role;
    private final String name;
    private final String nickname;
    private final String email;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
    private final boolean isDeleted;
}
