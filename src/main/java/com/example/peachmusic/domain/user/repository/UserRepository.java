package com.example.peachmusic.domain.user.repository;

import com.example.peachmusic.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 활성 상태(isDeleted=false)인 사용자 조회
    Optional<User> findByUserIdAndIsDeletedFalse(Long userId);
}
