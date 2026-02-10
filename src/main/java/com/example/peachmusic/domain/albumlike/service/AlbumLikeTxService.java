package com.example.peachmusic.domain.albumlike.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.albumlike.dto.response.AlbumLikeResponseDto;
import com.example.peachmusic.domain.albumlike.repository.AlbumLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlbumLikeTxService {

    private final AlbumLikeRepository albumLikeRepository;
    private final AlbumRepository albumRepository;

    /**
     * 앨범 좋아요 토글 기능
     *
     * @param authUser 인증된 사용자 정보
     * @param albumId  좋아요 토글할 앨범 ID
     * @return 토글 처리 결과(최종 좋아요 상태 및 좋아요 수)
     */
    @Transactional
    public AlbumLikeResponseDto doLikeAlbum(AuthUser authUser, Long albumId) {

        Long userId = authUser.getUserId();

        Album foundAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        int deleted = albumLikeRepository.deleteByAlbumIdAndUserId(albumId, userId);

        if (deleted == 1) {
            // 취소 성공 -> 카운트 -1 (원자 업데이트)
            albumRepository.decrementLikeCount(albumId);

            return buildResponse(albumId, foundAlbum.getAlbumName(), false);
        }

        int inserted = albumLikeRepository.insertIgnore(userId, albumId);

        if (inserted == 1) {
            albumRepository.incrementLikeCount(albumId);

            return buildResponse(albumId, foundAlbum.getAlbumName(), true);
        }
        // inserted == 0: 이미 좋아요가 존재함 (동시 요청으로 다른 요청이 먼저 생성했을 수 있음)
        return buildResponse(albumId, foundAlbum.getAlbumName(), true);
    }

    /**
     * 좋아요 수는 DB에서 직접 업데이트되므로
     * 응답 시점에 최신 값을 가져오기 위해 likeCount만 다시 조회
     */
    private AlbumLikeResponseDto buildResponse(Long albumId, String albumName, boolean liked) {
        Long likeCount = albumRepository.findLikeCountByAlbumId(albumId);

        if (likeCount == null) {
            throw new CustomException(ErrorCode.ALBUM_NOT_FOUND);
        }
        return AlbumLikeResponseDto.of(albumId, albumName, liked, likeCount);
    }

    public boolean isAlbumLiked(Long albumId, Long userId) {
        return albumLikeRepository.existsByAlbum_AlbumIdAndUser_UserId(albumId, userId);
    }
}
