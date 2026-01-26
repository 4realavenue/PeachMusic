package com.example.peachmusic.common.model;

import com.example.peachmusic.common.enums.UserRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class AuthUser {

    private final Long userId;
    private final String email;
    private final UserRole role;
    private final Long tokenVersion;

    public Collection<? extends GrantedAuthority> getAuthoritie() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
