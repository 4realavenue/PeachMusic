package com.example.peachmusic.domain.user.model.request;

import com.example.peachmusic.common.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserRoleChangeRequestDto {

    @NotBlank
    private UserRole role;  // "ADMIN" 또는 "USER"
}
