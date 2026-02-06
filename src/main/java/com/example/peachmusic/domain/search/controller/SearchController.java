package com.example.peachmusic.domain.search.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.search.dto.SearchPopularResponseDto;
import com.example.peachmusic.domain.search.dto.SearchPreviewResponseDto;
import com.example.peachmusic.domain.search.service.SearchHistoryService;
import com.example.peachmusic.domain.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SearchController {

    private final SearchHistoryService searchHistoryService;
    private final SearchService searchService;

    /**
     * 통합 검색 - 미리보기
     * @param word 검색어
     * @return 아티스트/앨범/음원 미리보기 응답
     */
    @GetMapping("/search")
    public ResponseEntity<CommonResponse<SearchPreviewResponseDto>> searchPreview(
            @RequestParam String word
    ) {
        SearchPreviewResponseDto result = searchService.searchPreview(word);
        return ResponseEntity.ok(CommonResponse.success("검색이 완료되었습니다.", result));
    }

    /**
     * 인기 검색어 조회
     * @return 인기 검색어 응답 DTO
     */
    @GetMapping("/search/popular")
    public ResponseEntity<CommonResponse<List<SearchPopularResponseDto>>> searchPopular() {
        List<SearchPopularResponseDto> result = searchHistoryService.searchPopular();
        return ResponseEntity.ok(CommonResponse.success("인기 검색어가 조회되었습니다.", result));
    }
}
