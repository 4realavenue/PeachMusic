package com.example.peachmusic.domain.playlist.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.playlist.dto.request.PlaylistCreateRequestDto;
import com.example.peachmusic.domain.playlist.dto.request.PlaylistUpdateRequestDto;
import com.example.peachmusic.domain.playlist.dto.response.PlaylistCreateResponseDto;
import com.example.peachmusic.domain.playlist.dto.response.PlaylistGetListResponseDto;
import com.example.peachmusic.domain.playlist.dto.response.PlaylistUpdateResponseDto;
import com.example.peachmusic.domain.playlist.entity.Playlist;
import com.example.peachmusic.domain.playlist.repository.PlaylistRepository;
import com.example.peachmusic.domain.playlistSong.repository.PlaylistSongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;

    /**
     * 플레이리스트 생성
     */
    @Transactional
    public PlaylistCreateResponseDto createPlaylist(PlaylistCreateRequestDto requestDto, AuthUser authUser) {

        Playlist playlist = new Playlist(authUser.getUser(), requestDto.getPlaylistName(), requestDto.getPlaylistImage());

        playlistRepository.save(playlist);

        return PlaylistCreateResponseDto.from(playlist);

    }

    /**
     * 플레이리스트 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PlaylistGetListResponseDto> getPlaylistAll(AuthUser authUser) {

        List<Playlist> findPlaylistList = playlistRepository.findAllByUser(authUser.getUser());

        return findPlaylistList.stream().map(PlaylistGetListResponseDto::from).toList();

    }

    /**
     * 플레이리스트 정보 수정
     */
    @Transactional
    public PlaylistUpdateResponseDto updatePlaylist(Long playlistId, PlaylistUpdateRequestDto requestDto, AuthUser authUser) {

        Playlist findPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYLIST_NOT_FOUND));

        if (!findPlaylist.isOwnedBy(authUser.getUserId())) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        findPlaylist.updatePlaylist(requestDto);

        return PlaylistUpdateResponseDto.from(findPlaylist);

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

}
