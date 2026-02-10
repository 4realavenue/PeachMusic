package com.example.peachmusic.domain.user.repository;

import com.example.peachmusic.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserCustomRepository {

    Optional<User> findUserByEmailAndIsDeletedFalse(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}
