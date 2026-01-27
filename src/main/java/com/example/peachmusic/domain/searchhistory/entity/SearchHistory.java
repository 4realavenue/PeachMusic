package com.example.peachmusic.domain.searchhistory.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "search_histories")
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "word", nullable = false)
    private String word; // 검색어

    @Column(name = "search_date", nullable = false)
    private LocalDate searchDate; // 검색 날짜

    @Column(name = "count", nullable = false)
    private Long count = 1L; // 검색 횟수

    public SearchHistory(String word, LocalDate searchDate) {
        this.word = word;
        this.searchDate = searchDate;
    }

    /**
     * 검색 횟수 증가
     */
    public void increaseCount() {
        this.count++;
    }
}
