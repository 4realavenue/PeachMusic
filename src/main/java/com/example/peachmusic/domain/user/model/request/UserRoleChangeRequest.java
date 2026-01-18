package com.example.peachmusic.domain.user.model.request;

import com.example.peachmusic.common.enums.UserRole;
import lombok.Getter;

@Getter
public class UserRoleChangeRequest {
    private UserRole role;  // "ADMIN" 또는 "USER"
}
