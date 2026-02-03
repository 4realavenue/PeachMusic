package com.example.peachmusic.domain.song.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CosineSimilarityTest {

    @Test
    @DisplayName("성공 - 같은 feature를 가진 벡터인 경우 내적 값 계산")
    void compute_success() {
        // given
        Map<String, Double> vector1 = Map.of(
                "g:pop", 0.6,
                "s:high", 0.8
        );

        Map<String, Double> vector2 = Map.of(
                "g:pop", 0.5,
                "s:high", 0.5
        );

        // when
        double result = CosineSimilarity.compute(vector1, vector2);

        // then
        // (0.6 * 0.5) + (0.8 * 0.5) = 0.3 + 0.4 = 0.7
        assertEquals(0.7, result);
    }

    @Test
    @DisplayName("실패 - 벡터가 비어있거나 겹치치 않을 경우 0.0 반환")
    void compute_fail() {
        // given
        Map<String, Double> vector1 = Map.of("g:pop", 1.0);
        Map<String, Double> vector2 = Map.of("g:rock", 1.0);

        // when
        double noOverlapResult = CosineSimilarity.compute(vector1, vector2);
        double emptyResult = CosineSimilarity.compute(vector1, Collections.emptyMap());

        // then
        assertEquals(0.0, noOverlapResult);
        assertEquals(0.0, emptyResult);
    }
}