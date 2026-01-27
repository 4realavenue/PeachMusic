package com.example.peachmusic.domain.genre.repository;

import com.example.peachmusic.domain.genre.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Long> {
}
