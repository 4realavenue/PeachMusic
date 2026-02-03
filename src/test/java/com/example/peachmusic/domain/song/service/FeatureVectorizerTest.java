package com.example.peachmusic.domain.song.service;

import com.example.peachmusic.domain.song.dto.SongFeatureDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureVectorizerTest {

    private final FeatureVectorizer featureVectorizer = new FeatureVectorizer();

    @Test
    @DisplayName("성공 - 장르 정보가 있으면 장르 feature가 생성")
    void vectorizeSong_Map_genreFeature() {
        // given
        SongFeatureDto dto = new SongFeatureDto(
                1L,
                List.of("Pop"),
                null,
                null,
                null
        );

        // when
        Map<String, Double> vector = featureVectorizer.vectorizeSongMap(dto);

        // then
        assertTrue(vector.containsKey("g:pop"));
    }

    @Test
    @DisplayName("성공 - 스피드 정보가 있으면 스피드 feature가 생성")
    void vectorizeSong_Map_speedFeature() {
        // given
        SongFeatureDto dto = new SongFeatureDto(
                1L,
                List.of(),
                "high",
                null,
                null
        );

        // when
        Map<String, Double> vector = featureVectorizer.vectorizeSongMap(dto);

        // then
        assertTrue(vector.containsKey("s:high"));
    }

    @Test
    @DisplayName("성공 - 태그 정보가 있으면 태그 feature가 생성")
    void vectorizeSong_Map_tagFeature() {
        // given
        SongFeatureDto dto = new SongFeatureDto(
                1L,
                List.of(),
                null,
                "happy, upbeat",
                null
        );

        // when
        Map<String, Double> vector = featureVectorizer.vectorizeSongMap(dto);

        // then
        assertTrue(
                vector.containsKey("t:happy") ||
                        vector.containsKey("t:upbeat")
        );
    }

    @Test
    @DisplayName("실패 - feature 정보가 없으면 빈 벡터를 반환")
    void vectorizeSong_Map_empty() {
        // given
        SongFeatureDto dto = new SongFeatureDto(
                1L,
                List.of(),
                null,
                null,
                null
        );

        // when
        Map<String, Double> vector = featureVectorizer.vectorizeSongMap(dto);

        // then
        assertTrue(vector.isEmpty());
    }

    @Test
    @DisplayName("성공 - 여러 음원이 있으면 사용자 벡터가 생성된다")
    void vectorizeUser_Map_success() {
        // given
        SongFeatureDto song1 = new SongFeatureDto(
                1L,
                List.of("Pop", "Rock"),
                "high",
                null,
                null
        );

        SongFeatureDto song2 = new SongFeatureDto(
                2L,
                List.of("Rock"),
                "high",
                null,
                null
        );

        List<SongFeatureDto> seedSongList = List.of(song1, song2);

        // when
        Map<String, Double> userVector = featureVectorizer.vectorizeUserMap(seedSongList);

        // then
        assertTrue(userVector.containsKey("g:pop"));
    }
}