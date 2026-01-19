package com.example.peachmusic.domain.playlist.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
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
     *
     * @param requestDto
     * @param userId     todo 인증/인가 들어오면 로그인 한 유저 받아오도록 수정 예정
     * @return
     */
    @Transactional
    public PlaylistCreateResponseDto createPlaylist(PlaylistCreateRequestDto requestDto, Long userId) {

        // 1. 플레이리스트를 만드는 유저 찾아오기
        //    todo 인증/인가 들어오면 로그인 한 유저의 정보 받아오도록 수정 예정
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 새로운 플레이리스트 생성
        Playlist playlist = new Playlist(findUser, requestDto.getPlaylistName());

        // 3. 생성 한 플레이리스트 저장
        playlistRepository.save(playlist);

        // 4. 응답dto의 from 메서드 실행 후 반환
        return PlaylistCreateResponseDto.from(playlist);

    }

    /**
     * 플레이리스트 목록 조회
     *
     * @param userId todo 인증/인가 들어오면 로그인 한 유저 받아오도록 수정 예정
     * @return
     */
    @Transactional(readOnly = true)
    public List<PlaylistGetListResponseDto> getPlaylistAll(Long userId) {

        // 1. 유저 찾아오기
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 찾아온 유저로 플레이리스트 리스트에 담아서 찾아옴
        List<Playlist> findPlaylistList = playlistRepository.findAllByUser(findUser);

        // 3. 찾아온 플레이리스트 리스트를 순회하면서 응답dto의 from 메서드를 실행 시키고
        //    리스트화 한 데이터를 반환
        return findPlaylistList.stream().map(PlaylistGetListResponseDto::from).toList();

    }

    /**
     * 플레이리스트 음원 조회
     *
     * @param playlistId
     * @return
     */
    @Transactional(readOnly = true)
    public PlaylistGetSongResponseDto getPlaylistSongList(Long playlistId) {

        // 1. 플레이리스트 아이디로 플레이리스트 찾아옴
        Playlist findPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYLIST_NOT_FOUND));

        // 2. 플레이리스트가 갖고 있는 음원들 리스트에 담아옴
        List<PlaylistSong> findPlaylistSong = playlistSongRepository.findAllByPlaylist(findPlaylist);

        // 3. 음원들 리스트를 순회하면서 응답dto의 내부 클래스인 응답 데이터 dto의 from 메서드를 실행 시키고
        //    응답 데이터 dto 리스트에 담아줌
        List<PlaylistGetSongResponseDto.PlaylistSongResponseDto> playlistSongDtoList = findPlaylistSong.stream().map(PlaylistGetSongResponseDto.PlaylistSongResponseDto::from).toList();

        // 4. 응답dto의 메서드 from 메서드 실행 후 반환
        return PlaylistGetSongResponseDto.from(findPlaylist, playlistSongDtoList);

    }

    /**
     * 플레이리스트 정보 수정
     *
     * @param playlistId
     * @param requestDto
     * @return
     */
    @Transactional
    public PlaylistUpdateResponseDto updatePlaylist(Long playlistId, PlaylistUpdateRequestDto requestDto) {

        // 1. 요청 받은 플레이리스트 아이디로 플레이리스트 찾아오기
        Playlist findPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYLIST_NOT_FOUND));

        // 2. 찾아온 플레이리스트의 updatePlaylist 메서드 실행
        //    RequestBody로 받아온 playlistName으로 갈아 끼우기
        findPlaylist.updatePlaylist(requestDto.getPlaylistName());

        // 3. 응답dto의 from 메서드 실행 후 반환
        return PlaylistUpdateResponseDto.from(findPlaylist);

    }

    /**
     * 플레이리스트 삭제
     *
     * @param playlistId
     */
    @Transactional
    public void deletePlaylist(Long playlistId) {

        // 1. 요청 받은 플레이리스트 아이디로 플레이리스트 찾아오기
        Playlist findPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYLIST_NOT_FOUND));

        // 2. 연관관계로 묶여있어서 플레이리스트가 바로 삭제 안됨
        //    찾아온 플레이리스트에 있는 음원들 전부 삭제
        playlistSongRepository.deleteAllByPlaylist(findPlaylist);

        // 3. 찾아온 플레이리스트 삭제
        playlistRepository.delete(findPlaylist);

    }

}
