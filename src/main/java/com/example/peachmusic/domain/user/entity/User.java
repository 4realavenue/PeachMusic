package com.example.peachmusic.domain.user.entity;

import com.example.peachmusic.common.entity.BaseEntity;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.domain.user.dto.request.UserUpdateRequestDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@AllArgsConstructor
@Builder(toBuilder = true)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.USER;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @NotNull
    @Column(name = "token_version", nullable = false)
    private Long tokenVersion = 0L;

    @Column(nullable = false)
    private boolean emailVerified = false;

    public void verifyEmail() {
        if (emailVerified) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }
        this.emailVerified = true;
    }

    public void increaseTokenVersion() {
        this.tokenVersion++;
    }

    public User(String name, String nickname, String email, String password) {
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.role = UserRole.USER;
    }

    public void update(UserUpdateRequestDto request) {
        if (isNotBlank(request.getName())) {
            this.name = request.getName().trim();
        }
        if (isNotBlank(request.getNickname())) {
            this.nickname = request.getNickname().trim();
        }
    }
    private static boolean isNotBlank(String str) {return str != null && !str.trim().isBlank();}
    public void setRole(UserRole role) {
        this.role = role != null ? role : UserRole.USER;
    }
    public void delete() {
        this.isDeleted = true;
    }
    public void restore() {
        this.isDeleted = false;
    }
}
