package com.example.peachmusic.domain.artist.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.model.response.ArtistGetDetailResponseDto;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artist.model.response.ArtistSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;

    /**
     * 아티스트 단건 조회 기능
     * @param artistId 조회할 아티스트 ID
     * @return 조회한 아티스트 정보
     */
    @Transactional(readOnly = true)
    public ArtistGetDetailResponseDto getArtistDetail(Long artistId) {

        // 조회 대상 아티스트 조회 (삭제된 아티스트는 조회 불가)
        Artist foundArtist = artistRepository.findByArtistIdAndIsDeletedFalse(artistId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_NOT_FOUND));

        return ArtistGetDetailResponseDto.from(foundArtist);
    }

    /**
     * 아티스트 검색 - 자세히 보기
     * @param word 검색어
     * @param pageable 페이징 정보 - 인기순 정렬
     * @return 페이징된 아티스트 검색 응답 DTO
     */
    @Transactional(readOnly = true)
    public Page<ArtistSearchResponse> searchArtistPage(String word, Pageable pageable) {
        return artistRepository.findArtistPageByWord(word, pageable);
    }

    /**
     * 아티스트 검색 - 미리보기
     * @param word 검색어
     * @return 아티스트 검색 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ArtistSearchResponse> searchArtistList(String word) {
        final int limit = 5;
        return artistRepository.findArtistListByWord(word, limit);
    }
}
