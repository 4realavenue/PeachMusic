package com.example.peachmusic.domain.user.dto.response;

import com.example.peachmusic.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDto {

    private String token;
    private UserRole role;
}
