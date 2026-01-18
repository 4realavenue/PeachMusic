package com.example.peachmusic.domain.playlist.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
import com.example.peachmusic.domain.playlist.entity.Playlist;
import com.example.peachmusic.domain.playlist.model.PlaylistDto;
import com.example.peachmusic.domain.playlist.model.request.PlaylistCreateRequestDto;
import com.example.peachmusic.domain.playlist.model.response.PlaylistCreateResponseDto;
import com.example.peachmusic.domain.playlist.model.response.PlaylistGetAllResponseDto;
import com.example.peachmusic.domain.playlist.repository.PlaylistRepository;
import com.example.peachmusic.domain.playlistSong.entity.PlaylistSong;
import com.example.peachmusic.domain.playlistSong.repository.PlaylistSongRepository;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * 플레이리스트 목록 조회
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    public PlaylistGetAllResponseDto getPlaylistAll(Long userId) {

        // 1. 유저 찾아오기
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 찾아온 유저로 플레이리스트 리스트에 담아서 찾아옴
        List<Playlist> findPlaylistList = playlistRepository.findAllByUser(findUser);

        // 3. 응답 데이터 Dto 리스트 생성
        List<PlaylistDto> playlistDtoList = new ArrayList<>();

        // 4. 찾아온 플레이리스트 리스트 순회
        //    응답 데이터 Dto 객체 생성하고 응답Dto 리스트에 추가
        for (Playlist p : findPlaylistList) {
            PlaylistDto playlistDto = PlaylistDto.from(p);
            playlistDtoList.add(playlistDto);
        }

        // 5. 응답 Dto 리스트 생성
        List<PlaylistGetAllResponseDto.PlaylistResponseDto> responseDtoList = new ArrayList<>();

        // 6. 응답 데이터 리스트 순회
        //    응답 Dto 객체 생성하고 응답 Dto 리스트에 추가
        for (PlaylistDto playlistDto : playlistDtoList) {
            PlaylistGetAllResponseDto.PlaylistResponseDto responseDto = PlaylistGetAllResponseDto.PlaylistResponseDto.from(playlistDto);
            responseDtoList.add(responseDto);
        }

        return PlaylistGetAllResponseDto.from(responseDtoList);
    }

}
