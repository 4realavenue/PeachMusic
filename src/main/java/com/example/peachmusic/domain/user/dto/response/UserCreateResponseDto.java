package com.example.peachmusic.domain.user.dto.response;

import com.example.peachmusic.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class UserCreateResponseDto {
    private Long userId;
    private String email;
    private String nickname;

    public UserCreateResponseDto(Long userId, String email, String nickname) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
    }

    public static UserCreateResponseDto from(User user) {
        return new UserCreateResponseDto(
                user.getUserId(),
                user.getEmail(),
                user.getNickname()
        );
    }
}