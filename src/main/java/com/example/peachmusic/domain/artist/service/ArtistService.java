package com.example.peachmusic.domain.artist.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.dto.response.ArtistGetDetailResponseDto;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import com.example.peachmusic.domain.artistLike.repository.ArtistLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static com.example.peachmusic.common.enums.UserRole.USER;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final ArtistLikeRepository artistLikeRepository;

    /**
     * 아티스트 단건 조회 기능
     * @param artistId 조회할 아티스트 ID
     * @return 조회한 아티스트 정보
     */
    @Transactional(readOnly = true)
    public ArtistGetDetailResponseDto getArtistDetail(AuthUser authUser, Long artistId) {

        boolean isLiked = false;

        if (authUser != null) {
            Long userId = authUser.getUserId();
            isLiked = artistLikeRepository.existsByArtist_ArtistIdAndUser_UserId(userId, artistId);
        }

        Artist foundArtist = artistRepository.findByArtistIdAndIsDeleted(artistId, false)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_DETAIL_NOT_FOUND));

        return ArtistGetDetailResponseDto.from(foundArtist, isLiked);
    }

    /**
     * 아티스트 검색 - 자세히 보기
     * @param word 검색어
     * @param pageable 페이징 정보 - 인기순 정렬
     * @return 페이징된 아티스트 검색 응답 DTO
     */
    @Transactional(readOnly = true)
    public Page<ArtistSearchResponseDto> searchArtistPage(String word, Pageable pageable) {
        return artistRepository.findArtistPageByWord(word, pageable, USER);
    }

    /**
     * 아티스트 검색 - 미리보기
     * @param word 검색어
     * @return 아티스트 검색 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ArtistSearchResponseDto> searchArtistList(String word) {
        final int limit = 5;
        return artistRepository.findArtistListByWord(word, limit);
    }
}
