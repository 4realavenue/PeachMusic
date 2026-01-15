package com.example.peachmusic.domain.searchWord.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "search_words")
public class SearchWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_word_id")
    private Long searchWordId;

    @Column(name = "word", nullable = false, unique = true)
    private String word;

    @Column(name = "count", nullable = false)
    private Long count;

    public SearchWord(String word, Long count) {
        this.word = word;
        this.count = count;
    }
}
