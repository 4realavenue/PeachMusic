package com.example.peachmusic.domain.albumlike.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.albumlike.entity.AlbumLike;
import com.example.peachmusic.domain.albumlike.dto.response.AlbumLikeResponseDto;
import com.example.peachmusic.domain.albumlike.repository.AlbumLikeRepository;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlbumLikeService {

    private final AlbumLikeRepository albumLikeRepository;
    private final AlbumRepository albumRepository;
    private final UserService userService;

    /**
     * 앨범 좋아요 토글 기능
     *
     * @param authUser 인증된 사용자 정보
     * @param albumId  좋아요 토글할 앨범 ID
     * @return 토글 처리 결과(최종 좋아요 상태 및 좋아요 수)
     */
    @Transactional
    public AlbumLikeResponseDto likeAlbum(AuthUser authUser, Long albumId) {

        User findUser = userService.findUser(authUser);
        Long userId = authUser.getUserId();

        Album foundAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));


        int deleted = albumLikeRepository.deleteByAlbumIdAndUserId(albumId, userId);

        if (deleted == 1) {
            albumRepository.decrementLikeCount(albumId);

            return buildResponse(albumId, foundAlbum.getAlbumName(), false);
        }

        try {
            albumLikeRepository.save(new AlbumLike(findUser, foundAlbum));
            albumRepository.incrementLikeCount(albumId);

            return buildResponse(albumId, foundAlbum.getAlbumName(), true);

        } catch (DataIntegrityViolationException e) {

            int corrected = albumLikeRepository.deleteByAlbumIdAndUserId(albumId, userId);
            if (corrected == 1) {
                albumRepository.decrementLikeCount(albumId);
            }
            return buildResponse(albumId, foundAlbum.getAlbumName(), false);
        }
    }

    private AlbumLikeResponseDto buildResponse(Long albumId, String albumName, boolean liked) {
        Long likeCount = albumRepository.findLikeCountByAlbumId(albumId);

        if (likeCount == null) {
            throw new CustomException(ErrorCode.ALBUM_NOT_FOUND);
        }

        return AlbumLikeResponseDto.of(albumId, albumName, liked, likeCount);
    }
}