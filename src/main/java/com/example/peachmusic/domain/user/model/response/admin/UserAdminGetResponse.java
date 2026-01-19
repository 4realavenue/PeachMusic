package com.example.peachmusic.domain.user.model.response.admin;


import com.example.peachmusic.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class UserAdminGetResponse {

    private final Long userId;
    private final String name;
    private final String nickname;
    private final String email;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public static UserAdminGetResponse from(User user) {
        return new UserAdminGetResponse(
                user.getUserId(),
                user.getName(),
                user.getNickname(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getModifiedAt()
        );
    }
}
