package com.example.peachmusic.domain.song.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.model.SongDto;
import com.example.peachmusic.domain.song.model.response.SongGetDetailResponseDto;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.songGenre.entity.SongGenre;
import com.example.peachmusic.domain.songGenre.repository.SongGenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;
    private final SongGenreRepository songGenreRepository;

    /**
     * 음원 단건 조회
     * @param songId
     * @return
     */
    @Transactional (readOnly = true)
    public SongGetDetailResponseDto getSong(Long songId) {

        // 1. 요청 받은 songId로 음원 찾아오기
        Song findSong = songRepository.findBySongIdAndIsDeletedFalse(songId)
                .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        // 2. 찾아온 음원의 앨범 id값 가져오기
        Long findSongAlbumId = findSong.getAlbum().getAlbumId();

        // 3. 찾아온 음원으로 중간 테이블에 저장 되어 있는 모든 데이터 리스트에 담아서 찾아오기
        List<SongGenre> findSongGenreList = songGenreRepository.findAllBySong(findSong);

        // 4. HTTP 응답 시에 사용 할 장르 이름 리스트 생성
        List<String> genreNameList = new ArrayList<>();

        // 5. 반복문으로 찾아온 중간 테이블 리스트 순회 하면서
        //    장르 이름 리스트에 장르 이름 담아줌
        for (SongGenre sg : findSongGenreList) {
            genreNameList.add(sg.getGenre().getGenreName());
        }

        SongDto songDto = new SongDto(findSong.getSongId(), findSongAlbumId, findSong.getName(), findSong.getDuration(), findSong.getLicenseCcurl(), findSong.getPosition(), findSong.getAudio(), findSong.getVocalinstrumental(), findSong.getLang(), findSong.getSpeed(), findSong.getInstruments(), findSong.getVartags(), findSong.getLikeCount(), findSong.getCreatedAt(), findSong.getModifiedAt());

        return SongGetDetailResponseDto.from(songDto, genreNameList);

    }


}
