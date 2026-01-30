package com.example.peachmusic.domain.artistlike.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artistlike.dto.response.ArtistLikeResponseDto;
import com.example.peachmusic.domain.artistlike.repository.ArtistLikeRepository;
import com.example.peachmusic.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.hibernate.AssertionFailure;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class ArtistLikeService {

    private final ArtistLikeRepository artistLikeRepository;
    private final ArtistRepository artistRepository;
    private final UserService userService;

    /**
     * 아티스트 좋아요 토글 기능
     *
     * @param authUser 인증된 사용자 정보
     * @param artistId 좋아요 토글할 아티스트 ID
     * @return 토글 처리 결과(최종 좋아요 상태 및 좋아요 수)
     */
    @Transactional
    public ArtistLikeResponseDto likeArtist(AuthUser authUser, Long artistId) {
        return retryOnLock(() -> doLikeArtist(authUser, artistId));
    }

    private ArtistLikeResponseDto doLikeArtist(AuthUser authUser, Long artistId) {

        userService.findUser(authUser);
        Long userId = authUser.getUserId();

        Artist foundArtist = artistRepository.findByArtistIdAndIsDeleted(artistId, false)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_NOT_FOUND));

        int deleted = artistLikeRepository.deleteByArtistIdAndUserId(artistId, userId);

        if (deleted == 1) {
            // 취소 성공 -> 카운트 -1 (원자 업데이트)
            artistRepository.decrementLikeCount(artistId);

            return buildResponse(artistId, foundArtist.getArtistName(), false);
        }

        int inserted = artistLikeRepository.insertIgnore(userId, artistId);

        if (inserted == 1) {
            artistRepository.incrementLikeCount(artistId);

            return buildResponse(artistId, foundArtist.getArtistName(), true);
        }

        return buildResponse(artistId, foundArtist.getArtistName(), true);
    }

    /**
     * 좋아요 수는 DB에서 직접 업데이트되므로
     * 응답 시점에 최신 값을 가져오기 위해 likeCount만 다시 조회
     */
    private ArtistLikeResponseDto buildResponse(Long artistId, String artistName, boolean liked) {
        Long likeCount = artistRepository.findLikeCountByArtistId(artistId);

        if (likeCount == null) {
            throw new CustomException(ErrorCode.ARTIST_NOT_FOUND);
        }

        return ArtistLikeResponseDto.of(artistId, artistName, liked, likeCount);
    }

    private ArtistLikeResponseDto retryOnLock(Supplier<ArtistLikeResponseDto> action) {
        int maxRetry = 2;

        for (int i = 0; i <= maxRetry; i++) {
            try {
                return action.get();
            } catch (PessimisticLockingFailureException e) {
                if (i == maxRetry) {
                    throw e;
                }
            }
        }
        throw new AssertionFailure("unreachable");
    }
}
