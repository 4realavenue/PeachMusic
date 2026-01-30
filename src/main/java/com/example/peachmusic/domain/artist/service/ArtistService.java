package com.example.peachmusic.domain.artist.service;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.Cursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.dto.response.ArtistGetDetailResponseDto;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import com.example.peachmusic.domain.artistlike.repository.ArtistLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static com.example.peachmusic.common.enums.SortDirection.DESC;
import static com.example.peachmusic.common.enums.SortType.NAME;
import static com.example.peachmusic.common.enums.SortType.LIKE;
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

        Artist foundArtist = artistRepository.findByArtistIdAndIsDeleted(artistId, false)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_DETAIL_NOT_FOUND));

        boolean isLiked = false;

        if (authUser != null) {
            Long userId = authUser.getUserId();
            isLiked = artistLikeRepository.existsByArtist_ArtistIdAndUser_UserId(artistId, userId);
        }

        return ArtistGetDetailResponseDto.from(foundArtist, isLiked);
    }

    /**
     * 아티스트 검색 - 자세히 보기
     */
    @Transactional(readOnly = true)
    public KeysetResponse<ArtistSearchResponseDto> searchArtistPage(String word, SortType sortType, SortDirection direction, Long lastId, Long lastLike, String lastName) {
        final int size = 10;

        // 커서 검증
        boolean missingLastLike = sortType == LIKE && lastId != null && lastLike == null;
        boolean missingLastName = sortType == NAME && lastId != null && lastName == null;
        if (missingLastLike || missingLastName) {
            throw new CustomException(ErrorCode.MISSING_CURSOR_PARAMETER);
        }

        // 아티스트 조회
        List<ArtistSearchResponseDto> result = artistRepository.findArtistKeysetPageByWord(word, USER, size, sortType, direction, lastId, lastLike, lastName);

        boolean hasNext = result.size() > size; // 다음 페이지 존재 여부
        Cursor nextCursor = null; // 다음 커서
        if (hasNext) {
            result.remove(size); // 다음 페이지 삭제

            // 다음 커서에 마지막 데이터 저장
            ArtistSearchResponseDto last = result.get(result.size() - 1);
            Long nextLastId = last.getArtistId();
            Long nextLastLike = sortType == LIKE ? last.getLikeCount() : null;
            String nextLastName = sortType == NAME ? last.getArtistName() : null;
            nextCursor = new Cursor(nextLastId, nextLastLike, nextLastName);
        }

        return new KeysetResponse<>(result, hasNext, nextCursor);
    }

    /**
     * 아티스트 검색 - 미리보기
     * @param word 검색어
     * @return 아티스트 검색 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ArtistSearchResponseDto> searchArtistList(String word) {
        final int size = 5;
        return artistRepository.findArtistListByWord(word, USER, size, LIKE, DESC); // 좋아요 많은 순
    }
}
