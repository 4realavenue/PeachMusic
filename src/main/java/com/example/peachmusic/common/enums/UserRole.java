package com.example.peachmusic.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    BOSS("ROLE_BOSS", "최고 권한"),
    ADMIN("ROLE_ADMIN", "관리자 권한"),
    USER("ROLE_USER", "일반 사용자 권한")
    ;

    private final String role; // 권한
    private final String description; // 설명
}