package com.example.peachmusic.domain.search.repository;

import com.example.peachmusic.domain.search.entity.Search;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface SearchRepository extends JpaRepository<Search, Long>, SearchCustomRepository {

    Optional<Search> findByWordAndSearchDate(String word, LocalDate searchDate);

}
