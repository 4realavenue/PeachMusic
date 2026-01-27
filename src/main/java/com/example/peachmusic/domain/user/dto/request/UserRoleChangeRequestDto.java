package com.example.peachmusic.domain.user.dto.request;

import com.example.peachmusic.common.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserRoleChangeRequestDto {

    @NotBlank
    private UserRole role;  // "ADMIN" 또는 "USER"
}
