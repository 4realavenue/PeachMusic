package com.example.peachmusic.domain.ranking.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RankingDto {

    private String title;
    private double score;
}