package com.example.peachmusic.domain.searchWord.repository;

import com.example.peachmusic.domain.searchWord.entity.SearchWord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchWordRepository extends JpaRepository<SearchWord, Long> {
}
