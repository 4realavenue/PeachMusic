package com.example.peachmusic.domain.searchHistory.repository;

import com.example.peachmusic.domain.searchHistory.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long>, SearchHistoryCustomRepository {

    Optional<SearchHistory> findByWordAndSearchDate(String word, LocalDate searchDate);

}
