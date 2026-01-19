package com.example.peachmusic.domain.user.model.response;

import com.example.peachmusic.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class UserGetResponseDto {

    private final Long userId;
    private final String name;
    private final String nickname;
    private final String email;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public static UserGetResponseDto from(User user) {
        return new UserGetResponseDto(user.getUserId(), user.getName(), user.getNickname(), user.getEmail(), user.getCreatedAt(), user.getModifiedAt()
        );
    }
}
