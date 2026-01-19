package com.example.peachmusic.domain.user.model.response.admin;


import com.example.peachmusic.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class UserAdminGetResponseDto {

    private final Long userId;
    private final String name;
    private final String nickname;
    private final String email;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public static UserAdminGetResponseDto from(User user) {
        return new UserAdminGetResponseDto(
                user.getUserId(),
                user.getName(),
                user.getNickname(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getModifiedAt()
        );
    }
}
