package com.example.peachmusic.domain.playlist.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
import com.example.peachmusic.domain.playlist.entity.Playlist;
import com.example.peachmusic.domain.playlist.model.PlaylistDto;
import com.example.peachmusic.domain.playlist.model.request.PlaylistCreateRequestDto;
import com.example.peachmusic.domain.playlist.model.request.PlaylistUpdateRequestDto;
import com.example.peachmusic.domain.playlist.model.response.PlaylistCreateResponseDto;
import com.example.peachmusic.domain.playlist.model.response.PlaylistGetAllResponseDto;
import com.example.peachmusic.domain.playlist.model.response.PlaylistGetSongResponseDto;
import com.example.peachmusic.domain.playlist.model.response.PlaylistUpdateResponseDto;
import com.example.peachmusic.domain.playlist.repository.PlaylistRepository;
import com.example.peachmusic.domain.playlistSong.entity.PlaylistSong;
import com.example.peachmusic.domain.playlistSong.model.response.PlaylistSongResponseDto;
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

    /**
     * 플레이리스트 음원 조회
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

        // 3. 응답 데이터 객체 담아줄 리스트 생성
        List<PlaylistSongResponseDto> playlistSongDtoList = new ArrayList<>();

        // 4. 찾아온 플레이리스트가 갖고 있는 음원 리스트 순회
        //    응답 데이터 객체 생성
        //    응답 데이터 객체 담아줄 리스트에 응답 데이터 객체 담아줌
        for (PlaylistSong ps : findPlaylistSong) {
            PlaylistSongResponseDto playlistSongDto = new PlaylistSongResponseDto(ps.getPlaylistSongId(), ps.getSong().getSongId(), ps.getSong().getName(), ps.getSong().getDuration(), ps.getSong().getLikeCount());
            playlistSongDtoList.add(playlistSongDto);
        }

        PlaylistDto playlistDto = PlaylistDto.from(findPlaylist);

        return PlaylistGetSongResponseDto.from(playlistDto, playlistSongDtoList);

    }

    /**
     * 플레이리스트 정보 수정
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

        PlaylistDto playlistDto = PlaylistDto.from(findPlaylist);

        return PlaylistUpdateResponseDto.from(playlistDto);

    }

}
