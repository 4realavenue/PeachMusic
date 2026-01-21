package com.example.peachmusic.domain.user.repository;

import com.example.peachmusic.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 활성 상태(isDeleted=false)인 사용자 조회
    Optional<User> findByUserIdAndIsDeletedFalse(Long userId);
    Optional<User> findUserByEmailAndIsDeletedFalse(String email);

    @Query("""
            SELECT u FROM User u
            WHERE u.nickname = :word
            """)
    Page<User> findALLByWord(String word, Pageable pageable);

    boolean existsByNickname(String nickname);
}
