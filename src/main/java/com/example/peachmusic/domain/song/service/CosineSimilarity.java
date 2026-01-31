package com.example.peachmusic.domain.song.service;

import java.util.Map;

public class CosineSimilarity {

    // 코사인 유사도 계산 메서드 -> 두 벡터는 정규화 되어있음, 결과 값 범위(0.0~1.0)
    public static double compute(Map<String, Double> v1, Map<String, Double> v2) {
        // 하나라도 없거나 비어있으면 비교 안함
        if (v1 == null || v2 == null || v1.isEmpty() || v2.isEmpty()) {
            return 0.0;
        }

        // 항상 작은 벡터를 기준으로 loop → 성능 최적화
        if (v1.size() > v2.size()) {
            Map<String, Double> temp = v1;
            v1 = v2;
            v2 = temp;
        }

        // 벡터의 내적을 누적할 변수
        double dot = 0.0;

        // v1의 모든 feature(key)를 기준으로 반복
        for (String key : v1.keySet()) {
            // v1의 feature key가 v2에도 존재하는지 확인
            Double value2 = v2.get(key);
            // 두 벡터 모두 존재하는 feature일때만 내적 계산
            if (value2 != null) {
                // 내적 계산 -> vl * v2
                dot += v1.get(key) * value2;
            }
        }
        return dot;
    }
}