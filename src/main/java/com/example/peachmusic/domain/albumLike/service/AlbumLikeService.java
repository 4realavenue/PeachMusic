package com.example.peachmusic.domain.albumLike.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.albumLike.entity.AlbumLike;
import com.example.peachmusic.domain.albumLike.model.response.AlbumLikeResponseDto;
import com.example.peachmusic.domain.albumLike.repository.AlbumLikeRepository;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlbumLikeService {

    private final AlbumLikeRepository albumLikeRepository;
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;

    /**
     * 앨범 좋아요 토글 기능
     *
     * @param authUser 인증된 사용자 정보
     * @param albumId 좋아요 토글할 앨범 ID
     * @return 토글 처리 결과(최종 좋아요 상태 및 좋아요 수)
     */
    @Transactional
    public AlbumLikeResponseDto likeAlbum(AuthUser authUser, Long albumId) {

        // AuthUser에서 사용자 ID 추출
        Long userId = authUser.getUserId();

        // 삭제되지 않은 유효한 사용자 여부 검증
        User foundUser = userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 좋아요 대상 앨범 조회 (삭제된 앨범은 좋아요 불가)
        Album foundAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        // 요청 전 좋아요 상태 확인
        boolean alreadyLiked = albumLikeRepository.existsByAlbum_AlbumIdAndUser_UserId(albumId, userId);

        // 이미 좋아요 상태면 취소
        if (alreadyLiked) {
            albumLikeRepository.deleteByAlbum_AlbumIdAndUser_UserId(albumId, userId);
            foundAlbum.decreaseLikeCount();
        } else {
            // 좋아요 상태가 아니면 등록
            albumLikeRepository.save(new AlbumLike(foundUser, foundAlbum));
            foundAlbum.increaseLikeCount();
        }

        // 처리 후 최종 좋아요 상태
        boolean liked = !alreadyLiked;

        return AlbumLikeResponseDto.of(foundAlbum.getAlbumId(), liked, foundAlbum.getLikeCount());
    }
}
