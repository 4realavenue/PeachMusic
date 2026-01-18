package com.example.peachmusic.domain.playlistSong.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
import com.example.peachmusic.domain.playlist.entity.Playlist;
import com.example.peachmusic.domain.playlist.repository.PlaylistRepository;
import com.example.peachmusic.domain.playlistSong.entity.PlaylistSong;
import com.example.peachmusic.domain.playlistSong.model.PlaylistSongDto;
import com.example.peachmusic.domain.playlistSong.model.request.PlaylistSongAddRequestDto;
import com.example.peachmusic.domain.playlistSong.model.response.PlaylistSongAddResponseDto;
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

    private final PlaylistSongRepository playlistSongRepository;
    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;

    @Transactional
    public PlaylistSongAddResponseDto addPlaylistSong(Long playlistId, PlaylistSongAddRequestDto requestDto) {

        // 1. 플레이리스트 찾아오기
        Playlist findPlaylist =  playlistRepository.findById(playlistId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYLIST_NOT_FOUND));

        // 2. 요청 받은 음원 아이디 담아오기
        List<Long> requestSongIdList = requestDto.getSongIds();

        // 3. 요청 받은 음원 아이디 리스트의 크기
        Integer addedCount = requestSongIdList.size();

        // 4. 음원 담아줄 리스트 생성
        List<Song> requestSongList = new ArrayList<>();

        // 5. 요청 받은 음원 리스트 순회
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

        // 6. 중간테이블 리스트 생성
        List<PlaylistSong> playlistSongList = new ArrayList<>();

        // 7. 요청 받은 음원 담아둔 리스트 순회
        //    새로운 중간테이블 객체 생성
        //    중간테이블 리스트에 객체 추가
        for (Song song : requestSongList) {
            PlaylistSong playlistSong = new PlaylistSong(song, findPlaylist);
            playlistSongList.add(playlistSong);
        }
        playlistSongRepository.saveAll(playlistSongList);

        return PlaylistSongAddResponseDto.from(findPlaylist.getPlaylistId(), requestSongIdList, addedCount);

    }
}
