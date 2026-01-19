package com.example.peachmusic.domain.playlist.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.playlist.entity.Playlist;
import com.example.peachmusic.domain.playlist.model.request.PlaylistCreateRequestDto;
import com.example.peachmusic.domain.playlist.model.request.PlaylistUpdateRequestDto;
import com.example.peachmusic.domain.playlist.model.response.PlaylistCreateResponseDto;
import com.example.peachmusic.domain.playlist.model.response.PlaylistGetListResponseDto;
import com.example.peachmusic.domain.playlist.model.response.PlaylistGetSongResponseDto;
import com.example.peachmusic.domain.playlist.model.response.PlaylistUpdateResponseDto;
import com.example.peachmusic.domain.playlist.repository.PlaylistRepository;
import com.example.peachmusic.domain.playlistSong.entity.PlaylistSong;
import com.example.peachmusic.domain.playlistSong.repository.PlaylistSongRepository;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final PlaylistSongRepository playlistSongRepository;

    /**
     * 플레이리스트 생성
     */
    @Transactional
    public PlaylistCreateResponseDto createPlaylist(PlaylistCreateRequestDto requestDto, AuthUser authUser) {

        // 1. 로그인 한 유저의 id 받아오기
        Long userId = authUser.getUserId();

        // 2. 로그인한 유저의 id로 유저 찾아오기
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 3. 새로운 플레이리스트 생성
        Playlist playlist = new Playlist(findUser, requestDto.getPlaylistName());

        // 4. 생성 한 플레이리스트 저장
        playlistRepository.save(playlist);

        // 5. 응답dto의 from 메서드 실행 후 반환
        return PlaylistCreateResponseDto.from(playlist);

    }

    /**
     * 플레이리스트 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PlaylistGetListResponseDto> getPlaylistAll(AuthUser authUser) {

        // 1. 로그인 한 유저의 id 받아오기
        Long userId = authUser.getUserId();

        // 2. 로그인 한 유저의 id로 유저 찾아오기
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 3. 찾아온 유저로 플레이리스트 리스트에 담아서 찾아옴
        List<Playlist> findPlaylistList = playlistRepository.findAllByUser(findUser);

        // 4. 찾아온 플레이리스트 리스트를 순회하면서 응답dto의 from 메서드를 실행 시키고
        //    리스트화 한 데이터를 반환
        return findPlaylistList.stream().map(PlaylistGetListResponseDto::from).toList();

    }

    /**
     * 플레이리스트 음원 조회
     */
    @Transactional(readOnly = true)
    public PlaylistGetSongResponseDto getPlaylistSongList(Long playlistId, AuthUser authUser) {

        // 1. 로그인 한 유저의 id 받아오기
        Long userId = authUser.getUserId();

        // 2. 플레이리스트 아이디로 플레이리스트 찾아옴
        Playlist findPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYLIST_NOT_FOUND));

        // 3. 찾아온 플레이리스트의 유저 아이디가 로그인 한 유저와 일치하지 않다면 예외처리
        if (!findPlaylist.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        // 4. 플레이리스트가 갖고 있는 음원들 리스트에 담아옴
        List<PlaylistSong> findPlaylistSong = playlistSongRepository.findAllByPlaylist(findPlaylist);

        // 5. 음원들 리스트를 순회하면서 응답dto의 내부 클래스인 응답 데이터 dto의 from 메서드를 실행 시키고
        //    응답 데이터 dto 리스트에 담아줌
        List<PlaylistGetSongResponseDto.PlaylistSongResponseDto> playlistSongDtoList = findPlaylistSong.stream().map(PlaylistGetSongResponseDto.PlaylistSongResponseDto::from).toList();

        // 6. 응답dto의 메서드 from 메서드 실행 후 반환
        return PlaylistGetSongResponseDto.from(findPlaylist, playlistSongDtoList);

    }

    /**
     * 플레이리스트 정보 수정
     */
    @Transactional
    public PlaylistUpdateResponseDto updatePlaylist(Long playlistId, PlaylistUpdateRequestDto requestDto, AuthUser authUser) {

        // 1. 로그인 한 유저의 id 받아오기
        Long userId = authUser.getUserId();

        // 2. 요청 받은 플레이리스트 아이디로 플레이리스트 찾아오기
        Playlist findPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYLIST_NOT_FOUND));

        // 3. 찾아온 플레이리스트의 유저 아이디가 로그인 한 유저와 일치하지 않다면 예외처리
        if (!findPlaylist.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        // 4. 찾아온 플레이리스트의 updatePlaylist 메서드 실행
        //    RequestBody로 받아온 playlistName으로 갈아 끼우기
        findPlaylist.updatePlaylist(requestDto.getPlaylistName());

        // 5. 응답dto의 from 메서드 실행 후 반환
        return PlaylistUpdateResponseDto.from(findPlaylist);

    }

    /**
     * 플레이리스트 삭제
     */
    @Transactional
    public void deletePlaylist(Long playlistId, AuthUser authUser) {

        // 1. 로그인 한 유저의 id 받아오기
        Long userId = authUser.getUserId();

        // 2. 요청 받은 플레이리스트 아이디로 플레이리스트 찾아오기
        Playlist findPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYLIST_NOT_FOUND));

        // 3. 찾아온 플레이리스트의 유저 아이디가 로그인 한 유저와 일치하지 않다면 예외처리
        if(!findPlaylist.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        // 4. 연관관계로 묶여있어서 플레이리스트가 바로 삭제 안됨
        //    찾아온 플레이리스트에 있는 음원들 전부 삭제
        playlistSongRepository.deleteAllByPlaylist(findPlaylist);

        // 3. 찾아온 플레이리스트 삭제
        playlistRepository.delete(findPlaylist);

    }

}
