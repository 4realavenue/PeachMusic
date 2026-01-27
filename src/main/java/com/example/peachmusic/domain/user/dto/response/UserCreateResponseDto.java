package com.example.peachmusic.domain.user.dto.response;

import com.example.peachmusic.domain.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserCreateResponseDto {
    private Long userId;
    private String email;
    private String nickname;

    public static UserCreateResponseDto from(User user, String token) {UserCreateResponseDto dto = new UserCreateResponseDto();
        dto.setUserId(user.getUserId());dto.setEmail(user.getEmail());dto.setNickname(user.getNickname());
        return dto;
    }
}
