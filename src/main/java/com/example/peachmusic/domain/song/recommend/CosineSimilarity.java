package com.example.peachmusic.domain.song.recommend;

import java.util.Map;

public class CosineSimilarity {

    // 코사인 유사도 계산 메서드 -> 두 벡터는 정규화 되어있음, 결과 값 범위(0.0~1.0)
    public static double compute(Map<String, Double> vector1, Map<String, Double> vector2) {
        // 하나라도 없거나 비어있으면 비교 안함
        if (vector1 == null || vector2 == null || vector1.isEmpty() || vector2.isEmpty()) {
            return 0.0;
        }

        // 항상 작은 벡터를 기준으로 loop → 성능 최적화
        if (vector1.size() > vector2.size()) {
            Map<String, Double> temp = vector1;
            vector1 = vector2;
            vector2 = temp;
        }

        // 벡터의 내적을 누적할 변수
        double dot = 0.0;

        // vector1의 모든 feature(key)를 기준으로 반복
        for (String key : vector1.keySet()) {
            // vector1의 feature key가 vector2에도 존재하는지 확인
            Double value2 = vector2.get(key);
            // 두 벡터 모두 존재하는 feature일때만 내적 계산
            if (value2 != null) {
                // 내적 계산 -> vector1 * vector2
                dot += vector1.get(key) * value2;
            }
        }
        return dot;
    }
}