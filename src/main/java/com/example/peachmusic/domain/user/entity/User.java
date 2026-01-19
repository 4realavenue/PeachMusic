package com.example.peachmusic.domain.user.entity;

import com.example.peachmusic.common.entity.BaseEntity;
import com.example.peachmusic.common.enums.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
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
    private UserRole role = UserRole.USER;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;


    public User(String name, String nickname, String email, String password) {
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.role = UserRole.USER;  // ← 명시적으로 기본값 지정
    }

    public void setRole(UserRole role) {
        this.role = role != null ? role : UserRole.USER;  // null 들어와도 USER로 방어
    }

    public void UpdateUser(String name, String nickname) {
        this.name = name;
        this.nickname = nickname;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public void restore() {
        this.isDeleted = false;
    }
}
