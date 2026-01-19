package com.example.peachmusic.domain.playlistSong.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
import com.example.peachmusic.domain.playlist.entity.Playlist;
import com.example.peachmusic.domain.playlist.repository.PlaylistRepository;
import com.example.peachmusic.domain.playlistSong.entity.PlaylistSong;
import com.example.peachmusic.domain.playlistSong.model.request.PlaylistSongAddRequestDto;
import com.example.peachmusic.domain.playlistSong.model.request.PlaylistSongDeleteRequestDto;
import com.example.peachmusic.domain.playlistSong.model.response.PlaylistSongAddResponseDto;
import com.example.peachmusic.domain.playlistSong.model.response.PlaylistSongDeleteSongResponseDto;
import com.example.peachmusic.domain.playlistSong.repository.PlaylistSongRepository;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistSongService {

    /**
     * 플레이리스트 음원 추가
     */
    private final PlaylistSongRepository playlistSongRepository;
    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;

    @Transactional
    public PlaylistSongAddResponseDto addPlaylistSong(Long playlistId, PlaylistSongAddRequestDto requestDto) {

        // 1. 플레이리스트 찾아오기
        Playlist findPlaylist =  playlistRepository.findById(playlistId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYLIST_NOT_FOUND));

        // 2. 요청이 유효한지 검증
        if (requestDto.getSongIds().isEmpty() || requestDto.getSongIds() == null) {
            throw new CustomException(ErrorCode.PLAYLIST_ADD_SONG_REQUIRED);
        }

        // 3. 요청 받은 음원 아이디 담아오기
        List<Long> requestSongIdList = requestDto.getSongIds();

        // 4. 요청 받은 음원 아이디 리스트의 크기
        Integer addedCount = requestSongIdList.size();

        // 5. 음원 담아줄 리스트 생성
        List<Song> requestSongList = new ArrayList<>();

        // 6. 요청 받은 음원 리스트 순회
        //    만약에 요청 받은 아이디가 이미 존재하면 중복 예외 처리
        //    그게 아니면 음원 아이디로 음원 찾아 옴
        //    음원 담아줄 리스트에 추가
        for (Long songId : requestSongIdList) {
            if (playlistSongRepository.existsByPlaylist_PlaylistIdAndSong_SongId(findPlaylist.getPlaylistId(), songId)) {
                throw new CustomException(ErrorCode.PLAYLIST_EXIST_SONG);
            }

            Song findSong = songRepository.findBySongIdAndIsDeletedFalse(songId)
                    .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

            requestSongList.add(findSong);
        }

        // 7. 중간테이블 리스트 생성
        List<PlaylistSong> playlistSongList = requestSongList.stream().map(song -> new PlaylistSong(song, findPlaylist)).toList();

        // 8. 중간테이블 레포지토리를 통해 DB에 중간테이블 리스트 전부 저장
        playlistSongRepository.saveAll(playlistSongList);

        // 9. 응답dto의 from 메서드를 실행 후 반환
        return PlaylistSongAddResponseDto.from(findPlaylist.getPlaylistId(), requestSongIdList, addedCount);

    }

    /**
     * 플레이리스트의 음원 삭제
     * @param playlistId
     * @param requestDto
     * @return
     */
    @Transactional
    public PlaylistSongDeleteSongResponseDto deletePlaylistSong(Long playlistId, PlaylistSongDeleteRequestDto requestDto) {

        // 1. 요청 받은 플레이리스트 아이디로 플레이리스트 찾아오기
        Playlist findPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYLIST_NOT_FOUND));

        // 2. 요청 받은 음원 아이디 담아오기
        List<Long> requestSongIdList = requestDto.getSongIds();

        // 3. 요청 받은 음원 아이디 리스트 순회
        //    만약에 플레이리스트 아이디와 음원 아이디를 가진 playlistSong이 없다면
        //    요청했던 플레이리스트에 해당 음원 없다고 예외 던짐
        //    조건에 부합하는 playlistSong이 있다면 삭제함
        for (Long songId : requestSongIdList) {
            if (!playlistSongRepository.existsByPlaylist_PlaylistIdAndSong_SongId(findPlaylist.getPlaylistId(), songId)) {
                throw new CustomException(ErrorCode.PLAYLIST_NOT_FOUND_SONG);
            }

            playlistSongRepository.deletePlaylistSongByPlaylist_PlaylistIdAndSong_SongId(findPlaylist.getPlaylistId(), songId);
        }

        // 4. 응답dto의 from 메서드 실행 후 반환
        return PlaylistSongDeleteSongResponseDto.from(findPlaylist, requestDto.getSongIds());

    }
}
