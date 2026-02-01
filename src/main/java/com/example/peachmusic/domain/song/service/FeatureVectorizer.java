package com.example.peachmusic.domain.song.service;

import com.example.peachmusic.domain.song.dto.SongFeatureDto;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 콘텐츠 기반 추천 Feature Vector 변환 컴포넌트
 * 1. 음원과 사용자의 데이터를 벡터 형태로 변환
 * 2. 코사인 유사도 기반 추천을 위한 전처리
 * 3. 휴리스틱 가중치를 적용한 벡터화 방식
 */
@Component
public class FeatureVectorizer {

    // 휴리스틱 기반 가중치 -> 장르 > 스피드 > 태그 > 악기 순으로 중요 => 총 합 1.0
    private static final double GENRE_WEIGHT = 0.4;
    private static final double SPEED_WEIGHT = 0.3;
    private static final double TAG_WEIGHT = 0.2;
    private static final double INSTRUMENT_WEIGHT = 0.1;

    // Song -> Feature vector 변환 메서드
    // 반환되는 Map은 <key, value> -> key:(ex. g:rock, t:happy, i:guitar), value:해당 feature 가중치
    public Map<String, Double> vectorizeSong(SongFeatureDto songFeatureDto) {
        // 반환하는 feature 벡터
        Map<String, Double> songVector = new HashMap<>();

        // Genre 정보 벡터화(g:), 장르가 여러개면 가중치를 균등하게 분배
        addGenres(songVector, songFeatureDto.getGenreNameList());

        // Tags 벡터화(t:), vartags: "happy, upbeat, summer" 같은 문자열을 쉼표로 분리 후 각 태기에 동일하게 가중치 분배
        addTokens(songVector, songFeatureDto.getVartags(), "t:", TAG_WEIGHT);

        // Instruments (i:), 기타, 피아노, 드럼 등, 태그랑 똑같이 처리
        addTokens(songVector, songFeatureDto.getInstruments(), "i:", INSTRUMENT_WEIGHT);

        // Speed (s:), verylow / low / normal / high / veryhigh, 단일 값이라 전체를 그대로 부여
        addSpeed(songVector, songFeatureDto.getSpeed());

        // L2 정규화 (코사인 유사도를 하려면 필수) 벡터의 크기를 1로 정규화 -> 코사인 유사도 계산의 전제 조건
        l2Normalize(songVector);
        return songVector;
    }

    // User Feature vector 변환 메서드 -> 좋아요와 플레이리스트 음원들의 벡터를 합산 후 사용자 취향 프로파일 생성
    public Map<String, Double> vectorizeUser(List<SongFeatureDto> seedSongList) {
        // User 취향 백터
        Map<String, Double> userVector = new HashMap<>();

        // 각 음원 벡터 누적
        for(SongFeatureDto songfeaturedto : seedSongList) {
            Map<String, Double> songVector = vectorizeSong(songfeaturedto);
            for(String key : songVector.keySet()) {
                double songValue = songVector.get(key);
                double songUserValue = userVector.getOrDefault(key, 0.0);

                // 기존 값 + 현재 음원 값 누적
                userVector.put(key, songValue + songUserValue);
            }
        }

        // 최종 유저 벡터 정규화
        l2Normalize(userVector);
        return userVector;
    }

    // 장르 벡터화 -> 장르가 2개면 각 장르에 GENRE_WEIGHT / 2씩 분재
    private void addGenres(Map<String, Double> vector, List<String> genreNameList) {
        // 장르 정보가 비어있으면 장르 feature 추가 안함
        if (genreNameList == null || genreNameList.isEmpty()) {
            return;
        }

        List<String> validGenreList = new ArrayList<>();

        // 유효한 장르만 필터링
        for(String name : genreNameList) {
            if(name != null && !name.isBlank()) {
                validGenreList.add(name.toLowerCase().trim());
            }
        }

        if(validGenreList.isEmpty()) {
            return;
        }

        // 장르개수만큼 장르 가중치를 분배
        double perGenreWeight = GENRE_WEIGHT / validGenreList.size();

        // 벡터에 최종 저장
        for (String validGenreName : validGenreList) {
            vector.put("g:" + validGenreName, perGenreWeight);
        }
    }

    // 태그, 악기 공통 처리 -> 문자열 기반 데이터여서 쉼표로 분리, 전체 가중치를 토큰의 수만큼 균등하게 분재
    private void addTokens(Map<String, Double> vector, String raw, String prefix, double totalWeight) {
        // 정보가 비어있으면 feature 추가 안함
        if (raw == null || raw.isBlank()) {
            return;
        }

        // 쉼표 기준으로 문자열 분리
        String[] tokens = raw.split(",");
        List<String> validTokeList = new ArrayList<>();

        for(String token : tokens) {
            String trimmed = token.toLowerCase().trim();
            if(!trimmed.isEmpty()) {
                validTokeList.add(trimmed);
            }
        }

        if(validTokeList.isEmpty()) {
            return;
        }

        // 토큰 가중치
        double tokenWeight = totalWeight / validTokeList.size();

        // 벡터에 최종 저장
        for (String validToken : validTokeList) {
            vector.put(prefix + validToken, tokenWeight);
        }
    }

    // 스피드 벡터화 -> 단일 카테고리 값이어서 분배 없이 그대로 가중치를 부여한다.
    private void addSpeed(Map<String, Double> vector, String speed) {
        if (speed == null || speed.isBlank()) {
            return;
        }
        // prefix를 붙여 feature key 생성
        vector.put("s:" + speed.toLowerCase().trim(), SPEED_WEIGHT);
    }

    // 정규화 -> 벡터의 길이를 1로 맞추는 메서드 -> 그러면 코사인 유사도는 단순하게 구현 가능
    // 공식 : v = v / ||v||
    private void l2Normalize(Map<String, Double> vector) {
        if(vector.isEmpty()) {
            return;
        }

        double l2NormSquared = 0.0;

        // 각 feature값의 제곱합 계산
        for (double value : vector.values()) {
            l2NormSquared += value * value;
        }

        // 백터의 크기 계산
        double vectorSize = Math.sqrt(l2NormSquared);
        if (vectorSize == 0.0) {
            return;
        }

        // 모든 값을 vectorSize으로 나눠서 정규화
        vector.replaceAll((key, value) -> value / vectorSize);
    }
}