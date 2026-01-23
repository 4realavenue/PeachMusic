package com.example.peachmusic.domain.user.dto.response.admin;

import com.example.peachmusic.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class UserUpdateResponseDto {

    private final Long userId;
    private final String name;
    private final String nickname;
    private final String email;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public static UserUpdateResponseDto from(User user) {
        return new UserUpdateResponseDto(user.getUserId(), user.getName(), user.getNickname(), user.getEmail(), user.getCreatedAt(), user.getModifiedAt()
        );
    }

}
