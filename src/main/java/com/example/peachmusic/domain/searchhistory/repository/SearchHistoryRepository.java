package com.example.peachmusic.domain.searchhistory.repository;

import com.example.peachmusic.domain.searchhistory.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long>, SearchHistoryCustomRepository {

    Optional<SearchHistory> findByWordAndSearchDate(String word, LocalDate searchDate);

}
