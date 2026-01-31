package com.example.peachmusic.domain.song.service;

import com.example.peachmusic.domain.song.dto.SongFeatureDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserFeatureVectorizer {

    private final SongFeatureVectorizer songFeatureVectorizer;

    public Map<String, Double> vectorizeUser(List<SongFeatureDto> seedSongList) {
        Map<String, Double> userVector = new HashMap<>();

        for(SongFeatureDto songfeaturedto : seedSongList) {
            Map<String, Double> songVector = songFeatureVectorizer.vectorizeSong(songfeaturedto);
            for(String key : songVector.keySet()) {
                double songValue = songVector.get(key);
                double songUserValue = userVector.getOrDefault(key, 0.0);
                userVector.put(key, songValue + songUserValue);
            }
        }
        l2Normalize(userVector);
        return userVector;
    }

    // 정규화 -> 벡터의 길이를 1로 맞추는 메서드 -> 그러면 코사인 유사도는 단순하게 구현 가능
    // 공식 : v = v / ||v||
    private void l2Normalize(Map<String, Double> vector) {
        if(vector.isEmpty()) {
            return;
        }

        double sumSquares = 0.0;
        // 각 feature값의 제곱합 계산
        for (double value : vector.values()) {
            sumSquares += value * value;
        }

        // 백터의 크기 계산
        double vectorSize = Math.sqrt(sumSquares);
        if (vectorSize == 0.0) {
            return;
        }

        // 모든 값을 vectorSize으로 나눠서 정규화
        vector.replaceAll((key, value) -> value / vectorSize);
    }
}