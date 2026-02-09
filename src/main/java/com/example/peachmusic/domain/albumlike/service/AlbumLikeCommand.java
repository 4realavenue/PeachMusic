package com.example.peachmusic.domain.albumlike.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.albumlike.dto.response.AlbumLikeResponseDto;
import com.example.peachmusic.domain.albumlike.repository.AlbumLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AlbumLikeCommand {

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

        boolean liked;

        int deleted = albumLikeRepository.deleteByAlbumIdAndUserId(albumId, userId);

        if (deleted == 1) {
            // 취소 성공 -> 카운트 -1 (원자 업데이트)
            albumRepository.decrementLikeCount(albumId);
            liked = false;
        } else {
            int inserted = albumLikeRepository.insertIgnore(userId, albumId);

            if (inserted == 1) {
                albumRepository.incrementLikeCount(albumId);
            }
            liked = true; // inserted==0 이어도 최종 상태는 좋아요(true)
        }

        Long likeCount = getAlbumLikeCount(albumId);
        return createAlbumLikeResponse(albumId, foundAlbum.getAlbumName(), liked, likeCount);
    }

    /**
     * 좋아요 수는 DB에서 직접 업데이트되므로
     * 응답 시점에 최신 값을 가져오기 위해 likeCount만 다시 조회
     */
    private Long getAlbumLikeCount(Long albumId) {
        Long likeCount = albumRepository.findLikeCountByAlbumId(albumId);

        if (likeCount == null) {
            throw new CustomException(ErrorCode.ALBUM_NOT_FOUND);
        }
        return likeCount;
    }

    private AlbumLikeResponseDto createAlbumLikeResponse(Long albumId, String albumName, boolean liked, Long likeCount) {
        return AlbumLikeResponseDto.of(albumId, albumName, liked, likeCount);
    }
}
