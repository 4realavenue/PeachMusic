package com.example.peachmusic.domain.playlist.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
import com.example.peachmusic.domain.playlist.entity.Playlist;
import com.example.peachmusic.domain.playlist.model.PlaylistDto;
import com.example.peachmusic.domain.playlist.model.request.PlaylistCreateRequestDto;
import com.example.peachmusic.domain.playlist.model.response.PlaylistCreateResponseDto;
import com.example.peachmusic.domain.playlist.repository.PlaylistRepository;
import com.example.peachmusic.domain.playlistSong.repository.PlaylistSongRepository;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final PlaylistSongRepository playlistSongRepository;

    /**
     * 플레이리스트 생성
     * @param requestDto
     * @param userId
     * @return
     */
    @Transactional
    // todo 인증/인가 들어오면 로그인 한 유저 받아오도록 수정 예정
    public PlaylistCreateResponseDto createPlaylist(PlaylistCreateRequestDto requestDto, Long userId) {

        // 1. 플레이리스트를 만드는 유저 찾아오기
        //    todo 인증/인가 들어오면 로그인 한 유저의 정보 받아오도록 수정 예정
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 새로운 플레이리스트 생성
        Playlist playlist = new Playlist(findUser, requestDto.getPlaylistName());

        //    todo userPlaylist 중간 테이블이 필요하지 않을까?
        // 3. 생성 한 플레이리스트 저장
        playlistRepository.save(playlist);

        PlaylistDto playlistDto = PlaylistDto.from(playlist);

        return PlaylistCreateResponseDto.from(playlistDto);
    }

}
