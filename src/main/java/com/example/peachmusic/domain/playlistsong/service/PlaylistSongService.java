package com.example.peachmusic.domain.playlistsong.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.playlist.dto.response.PlaylistGetSongResponseDto;
import com.example.peachmusic.domain.playlist.entity.Playlist;
import com.example.peachmusic.domain.playlist.repository.PlaylistRepository;
import com.example.peachmusic.domain.playlistsong.dto.request.PlaylistSongAddRequestDto;
import com.example.peachmusic.domain.playlistsong.dto.request.PlaylistSongDeleteRequestDto;
import com.example.peachmusic.domain.playlistsong.dto.response.PlaylistSongAddResponseDto;
import com.example.peachmusic.domain.playlistsong.dto.response.PlaylistSongDeleteSongResponseDto;
import com.example.peachmusic.domain.playlistsong.entity.PlaylistSong;
import com.example.peachmusic.domain.playlistsong.repository.PlaylistSongRepository;
import com.example.peachmusic.domain.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PlaylistSongService {


    private final PlaylistSongRepository playlistSongRepository;
    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;

    /**
     * 플레이리스트 음원 조회
     */
    @Transactional(readOnly = true)
    public PlaylistGetSongResponseDto getPlaylistSongList(Long playlistId, AuthUser authUser) {

        Long userId = authUser.getUserId();

        Playlist findPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYLIST_NOT_FOUND));

        if (!findPlaylist.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        List<PlaylistSong> findPlaylistSong = playlistSongRepository.findAllByPlaylist(findPlaylist);

        List<PlaylistGetSongResponseDto.SongResponseDto> playlistSongDtoList = findPlaylistSong.stream()
                .map(PlaylistGetSongResponseDto.SongResponseDto::from).toList();

        return PlaylistGetSongResponseDto.from(findPlaylist, playlistSongDtoList);

    }

    /**
     * 플레이리스트 음원 추가
     */
    @Transactional
    public PlaylistSongAddResponseDto addPlaylistSong(Long playlistId, PlaylistSongAddRequestDto requestDto, AuthUser authUser) {

        Long userId = authUser.getUserId();

        Playlist findPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYLIST_NOT_FOUND));

        if (!findPlaylist.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        if (requestDto.getSongIdList() == null || requestDto.getSongIdList().isEmpty()) {
            throw new CustomException(ErrorCode.PLAYLIST_ADD_SONG_REQUIRED);
        }

        Set<Long> validRequestSongIdSet = songRepository.findSongIdSetBySongIdList(requestDto.getSongIdList());

        Set<Long> duplicationSongIdSet = playlistSongRepository.findSongIdSetByPlaylist_PlaylistIdAndSong_SongIdList(findPlaylist.getPlaylistId(), validRequestSongIdSet);

        List<Long> validSongIdList = validRequestSongIdSet.stream()
                .filter(songId -> !duplicationSongIdSet.contains(songId)).toList();

        List<PlaylistSong> playlistSongList = validSongIdList.stream()
                .map(songRepository::getReferenceById).map(song -> new PlaylistSong(song, findPlaylist)).toList();

        playlistSongRepository.saveAll(playlistSongList);

        return PlaylistSongAddResponseDto.from(findPlaylist.getPlaylistId(), validSongIdList, validSongIdList.size());

    }

    /**
     * 플레이리스트의 음원 삭제
     */
    @Transactional
    public PlaylistSongDeleteSongResponseDto deletePlaylistSong(Long playlistId, PlaylistSongDeleteRequestDto requestDto, AuthUser authUser) {

        Long userId = authUser.getUserId();

        Playlist findPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYLIST_NOT_FOUND));

        if (!findPlaylist.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        if (requestDto.getSongIdList() == null || requestDto.getSongIdList().isEmpty()) {
            throw new CustomException(ErrorCode.PLAYLIST_REMOVE_SONG_REQUIRED);
        }

        Set<Long> validRequestSongIdSet = songRepository.findSongIdSetBySongIdList(requestDto.getSongIdList());

        Set<Long> duplicationSongIdSet = playlistSongRepository.findSongIdSetByPlaylist_PlaylistIdAndSong_SongIdList(findPlaylist.getPlaylistId(), validRequestSongIdSet);

        List<Long> validSongIdList = validRequestSongIdSet.stream()
                .filter(duplicationSongIdSet::contains).toList();

        playlistSongRepository.deletePlaylistSongByPlaylist_PlaylistIdAndSong_SongId(findPlaylist.getPlaylistId(), validSongIdList);

        return PlaylistSongDeleteSongResponseDto.from(findPlaylist, validSongIdList);

    }
}
