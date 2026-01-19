package com.example.peachmusic.domain.song.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.model.response.SongGetDetailResponseDto;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.songGenre.entity.SongGenre;
import com.example.peachmusic.domain.songGenre.repository.SongGenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;
    private final SongGenreRepository songGenreRepository;

    /**
     * 음원 단건 조회
     */
    @Transactional(readOnly = true)
    public SongGetDetailResponseDto getSong(Long songId) {

        // 1. 요청 받은 songId로 음원 찾아오기
        Song findSong = songRepository.findBySongIdAndIsDeletedFalse(songId)
                .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        // 2. 찾아온 음원으로 중간 테이블에 저장 되어 있는 모든 데이터 리스트에 담아서 찾아오기
        List<SongGenre> findSongGenreList = songGenreRepository.findAllBySong(findSong);

        // 3. 중간 테이블 리스트를 순회하면서 중간테이블->음원->음원이름을 추출해서 문자열 리스트로 변환
        List<String> genreNameList = findSongGenreList.stream().map(songGenre -> songGenre.getGenre().getGenreName()).toList();

        // 4. 응답dto의 from 메서드 실행 후 반환
        return SongGetDetailResponseDto.from(findSong, genreNameList);

    }
}
