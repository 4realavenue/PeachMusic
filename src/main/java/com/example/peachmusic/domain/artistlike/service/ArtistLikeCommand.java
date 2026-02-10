package com.example.peachmusic.domain.artistlike.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artistlike.dto.response.ArtistLikeResponseDto;
import com.example.peachmusic.domain.artistlike.repository.ArtistLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ArtistLikeCommand {

    private final ArtistLikeRepository artistLikeRepository;
    private final ArtistRepository artistRepository;

    /**
     * 아티스트 좋아요 토글 기능
     *
     * @param authUser 인증된 사용자 정보
     * @param artistId 좋아요 토글할 아티스트 ID
     * @return 토글 처리 결과(최종 좋아요 상태 및 좋아요 수)
     */
    @Transactional
    public ArtistLikeResponseDto doLikeArtist(AuthUser authUser, Long artistId) {

        Long userId = authUser.getUserId();

        Artist foundArtist = artistRepository.findByArtistIdAndIsDeleted(artistId, false)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_NOT_FOUND));

        boolean liked;

        int deleted = artistLikeRepository.deleteByArtistIdAndUserId(artistId, userId);

        if (deleted == 1) {
            artistRepository.decrementLikeCount(artistId);
            liked = false;
        } else {
            int inserted = artistLikeRepository.insertIgnore(userId, artistId);

            if (inserted == 1) {
                artistRepository.incrementLikeCount(artistId);
            }
            liked = true;
        }
        Long likeCount = getArtistLikeCount(artistId);
        return ArtistLikeResponseDto.of(artistId, foundArtist.getArtistName(), liked, likeCount);
    }

    private Long getArtistLikeCount(Long artistId) {
        Long likeCount = artistRepository.findLikeCountByArtistId(artistId);

        if (likeCount == null) {
            throw new CustomException(ErrorCode.ARTIST_NOT_FOUND);
        }
        return likeCount;
    }
}
