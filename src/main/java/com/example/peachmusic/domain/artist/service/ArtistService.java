package com.example.peachmusic.domain.artist.service;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.Cursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.service.AbstractKeysetService;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.dto.response.ArtistGetDetailResponseDto;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import com.example.peachmusic.domain.artistlike.repository.ArtistLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.function.Function;
import static com.example.peachmusic.common.enums.SortDirection.DESC;
import static com.example.peachmusic.common.enums.SortType.LIKE;

@Service
@RequiredArgsConstructor
public class ArtistService extends AbstractKeysetService {

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

        validateWord(word); // 단어 검증
        validateCursor(sortType, lastId, lastLike, lastName); // 커서 검증

        final int size = 10;
        final boolean isAdmin = false;
        direction = resolveSortDirection(sortType, direction);

        // 아티스트 조회
        List<ArtistSearchResponseDto> content = artistRepository.findArtistKeysetPageByWord(word, size, isAdmin, sortType, direction, lastId, lastLike, lastName);

        // 정렬 기준에 따라 커서 결정
        Function<ArtistSearchResponseDto, Cursor> cursorExtractor = switch (sortType) {
            case LIKE -> last -> new Cursor(last.getArtistId(), last.getLikeCount());
            case NAME -> last -> new Cursor(last.getArtistId(), last.getArtistName());
        };

        return toKeysetResponse(content, size, cursorExtractor);
    }

    /**
     * 아티스트 검색 - 미리보기
     * @param word 검색어
     * @return 아티스트 검색 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ArtistSearchResponseDto> searchArtistList(String word) {
        final int size = 5;
        final boolean isAdmin = false;
        return artistRepository.findArtistListByWord(word, size, isAdmin, LIKE, DESC);
    }
}
