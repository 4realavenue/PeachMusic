package com.example.peachmusic.domain.user.repository;

import com.example.peachmusic.domain.user.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

//    Optional<User> findByUserIdAndIsDeletedFalse(Long userId);
//    Optional<User> findByUserId(Long userId);
    Optional<User> findUserByEmailAndIsDeletedFalse(String email);

//    @Query("SELECT u FROM User u WHERE u.isDeleted = false")
    Page<User> findAll(Pageable pageable);

}
