package com.example.peachmusic.domain.playlist.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.enums.FileType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.storage.FileStorageService;
import com.example.peachmusic.domain.playlist.dto.request.PlaylistCreateRequestDto;
import com.example.peachmusic.domain.playlist.dto.request.PlaylistUpdateRequestDto;
import com.example.peachmusic.domain.playlist.dto.response.PlaylistCreateResponseDto;
import com.example.peachmusic.domain.playlist.dto.response.PlaylistGetListResponseDto;
import com.example.peachmusic.domain.playlist.dto.response.PlaylistImageUpdateResponseDto;
import com.example.peachmusic.domain.playlist.dto.response.PlaylistUpdateResponseDto;
import com.example.peachmusic.domain.playlist.entity.Playlist;
import com.example.peachmusic.domain.playlist.repository.PlaylistRepository;
import com.example.peachmusic.domain.playlistsong.repository.PlaylistSongRepository;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final FileStorageService fileStorageService;
    private final UserService userService;

    /**
     * 플레이리스트 생성
     */
    @Transactional
    public PlaylistCreateResponseDto createPlaylist(PlaylistCreateRequestDto requestDto, MultipartFile playlistImage, AuthUser authUser) {

        User findUser = userService.findUser(authUser);

        String playlistName = requestDto.getPlaylistName().trim();

        String storedPath = "https://img.peachmusics.com/storage/image/default-image.jpg";

        Playlist playlist = new Playlist(findUser, playlistName, storedPath);

        Playlist savedPlaylist = playlistRepository.save(playlist);

        if (playlistImage != null && !playlistImage.isEmpty()) {
            storedPath = storePlaylistImage(playlistImage, savedPlaylist.getPlaylistId());

            savedPlaylist.updatePlaylistImage(storedPath);
        }

        return PlaylistCreateResponseDto.from(savedPlaylist);

    }

    /**
     * 플레이리스트 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PlaylistGetListResponseDto> getPlaylistAll(AuthUser authUser) {

        User findUser = userService.findUser(authUser);

        List<Playlist> findPlaylistList = playlistRepository.findAllByUser(findUser);

        return findPlaylistList.stream().map(PlaylistGetListResponseDto::from).toList();

    }

    /**
     * 플레이리스트 기본 정보 수정
     */
    @Transactional
    public PlaylistUpdateResponseDto updatePlaylist(Long playlistId, PlaylistUpdateRequestDto requestDto, AuthUser authUser) {

        Playlist findPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYLIST_NOT_FOUND));

        if (!findPlaylist.isOwnedBy(authUser.getUserId())) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        findPlaylist.updatePlaylistName(requestDto);

        return PlaylistUpdateResponseDto.from(findPlaylist);

    }

    /**
     * 플레이리스트 이미지 수정
     */
    @Transactional
    public PlaylistImageUpdateResponseDto updatePlaylistImage(Long playlistId, MultipartFile playlistImage, AuthUser authUser) {

        Playlist findPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYLIST_NOT_FOUND));

        if (!findPlaylist.isOwnedBy(authUser.getUserId())) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        String oldPath = findPlaylist.getPlaylistImage();

        String newPath = storePlaylistImage(playlistImage, findPlaylist.getPlaylistId());

        findPlaylist.updatePlaylistImage(newPath);

        if (oldPath != null) {
            fileStorageService.deleteFileByPath(oldPath);
        }

        return PlaylistImageUpdateResponseDto.from(findPlaylist);
    }

    /**
     * 플레이리스트 삭제
     */
    @Transactional
    public void deletePlaylist(Long playlistId, AuthUser authUser) {

        Playlist findPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYLIST_NOT_FOUND));

        if (!findPlaylist.isOwnedBy(authUser.getUserId())) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        playlistSongRepository.deleteAllByPlaylist(findPlaylist);

        playlistRepository.delete(findPlaylist);

    }

    private String storePlaylistImage(MultipartFile playlistImage, Long playlistId) {
        String baseName = playlistId.toString();
        return fileStorageService.storeFile(playlistImage, FileType.PLAYLIST_IMAGE, baseName);
    }
}
