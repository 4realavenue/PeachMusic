package com.example.peachmusic.domain.user.repository;

import com.example.peachmusic.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByEmailAndIsDeletedFalse(String email);
    boolean existsByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE :word IS NULL OR u.nickname = :word
            """)
    Page<User> findALLByWord(String word, Pageable pageable);

    boolean existsByNickname(String nickname);
}
