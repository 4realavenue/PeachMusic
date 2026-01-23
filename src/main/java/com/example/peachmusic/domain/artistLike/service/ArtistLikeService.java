package com.example.peachmusic.domain.artistLike.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artistLike.entity.ArtistLike;
import com.example.peachmusic.domain.artistLike.dto.response.ArtistLikeResponseDto;
import com.example.peachmusic.domain.artistLike.repository.ArtistLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArtistLikeService {

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
    public ArtistLikeResponseDto likeArtist(AuthUser authUser, Long artistId) {

        Long userId = authUser.getUserId();

        Artist foundArtist = artistRepository.findByArtistIdAndIsDeletedFalse(artistId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_NOT_FOUND));

        // 요청 전 좋아요 상태 확인
        boolean alreadyLiked = artistLikeRepository.existsByArtist_ArtistIdAndUser_UserId(artistId, userId);

        // 이미 좋아요 상태면 취소
        if (alreadyLiked) {
            artistLikeRepository.deleteByArtist_ArtistIdAndUser_UserId(artistId, userId);
            foundArtist.decreaseLikeCount();
        } else {
            // 좋아요 상태가 아니면 등록
            artistLikeRepository.save(new ArtistLike(authUser.getUser(), foundArtist));
            foundArtist.increaseLikeCount();
        }

        // 처리 후 최종 좋아요 상태
        boolean liked = !alreadyLiked;

        return ArtistLikeResponseDto.of(foundArtist.getArtistId(), foundArtist.getArtistName(), liked, foundArtist.getLikeCount());
    }
}
