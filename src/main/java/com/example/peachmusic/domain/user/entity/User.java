package com.example.peachmusic.domain.user.entity;

import com.example.peachmusic.common.entity.BaseEntity;
import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.domain.user.dto.request.UserUpdateRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
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
    private UserRole role;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "token_version", nullable = false)
    private Long tokenVersion = 0L;

    @Column(nullable = false)
    private boolean emailVerified = false;

    public User(String name, String nickname, String email, String password) {
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.role = UserRole.USER;
    }

    public void increaseTokenVersion() {this.tokenVersion++;}

    public void update(UserUpdateRequestDto request) {
        if (request.getName() != null) {
            this.name = request.getName().trim();
        }
        if (request.getNickname() != null) {
            this.nickname = request.getNickname().trim();
        }
    }
    public void delete() {this.isDeleted = true;}
    public void restore() {this.isDeleted = false;}
    public void setRole(UserRole role) {this.role = role != null ? role : UserRole.USER;}
}
