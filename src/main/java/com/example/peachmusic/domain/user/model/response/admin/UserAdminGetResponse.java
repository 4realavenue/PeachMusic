package com.example.peachmusic.domain.user.model.response.admin;


import com.example.peachmusic.domain.user.model.UserDto;
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

    public static UserAdminGetResponse from(UserDto dto) {
        return new UserAdminGetResponse(
                dto.getUserId(),
                dto.getName(),
                dto.getNickname(),
                dto.getEmail(),
                dto.getCreatedAt(),
                dto.getModifiedAt()
        );
    }
}
