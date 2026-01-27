package com.example.peachmusic.domain.searchhistory.service;

import com.example.peachmusic.domain.searchhistory.entity.SearchHistory;
import com.example.peachmusic.domain.searchhistory.dto.SearchPopularResponseDto;
import com.example.peachmusic.domain.searchhistory.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final SearchHistoryRepository historyRepository;

    /**
     * 날짜마다 검색어 횟수 기록
     * - 오늘 날짜에 검색어가 존재하면 검색 횟수 증가
     * - 오늘 날짜에 검색어가 존재 안 하면 검색어 저장
     * @param word 검색어
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSearch(String word) {

        LocalDate today = LocalDate.now(); // 오늘 날짜

        historyRepository.findByWordAndSearchDate(word, today) // 오늘 날짜에 키워드 검색 조회
                .ifPresentOrElse(
                        SearchHistory::increaseCount, // 존재하면 검색 횟수 증가
                        () -> historyRepository.save(new SearchHistory(word, today)) // 존재 안 하면 검색 저장
                );
    }

    /**
     * 인기 검색어 조회
     * @return 인기 검색어 응답 DTO
     */
    @Transactional(readOnly = true)
    public List<SearchPopularResponseDto> searchPopular() {
        return historyRepository.findPopularKeyword();
    }
}
