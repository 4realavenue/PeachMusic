package com.example.peachmusic.domain.user.model;

import com.example.peachmusic.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserDto {

    private final Long userId;
    private final String name;
    private final String nickname;
    private final String email;
    private final String password;
    private final boolean isDeleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;


    public static UserDto from(User user) {
        return new UserDto(
                user.getUserId(),
                user.getName(),
                user.getNickname(),
                user.getEmail(),
                user.getPassword(),
                user.isDeleted(),
                user.getCreatedAt(),
                user.getModifiedAt()
        );
    }
}
