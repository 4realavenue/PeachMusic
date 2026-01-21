package com.example.peachmusic.domain.song.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.model.response.SongGetDetailResponseDto;
import com.example.peachmusic.domain.song.model.response.SongSearchResponse;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.songGenre.entity.SongGenre;
import com.example.peachmusic.domain.songGenre.repository.SongGenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static com.example.peachmusic.common.enums.UserRole.USER;

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

    /**
     * 음원 검색 - 자세히 보기
     * @param word 검색어
     * @param pageable 페이징 정보 - 인기순 정렬
     * @return 페이징된 음원 검색 응답 DTO
     */
    @Transactional(readOnly = true)
    public Page<SongSearchResponse> searchSongPage(String word, Pageable pageable) {
        return songRepository.findSongPageByWord(word, pageable, USER);
    }

    /**
     * 음원 검색 - 미리보기
     * @param word 검색어
     * @return 음원 검색 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<SongSearchResponse> searchSongList(String word) {
        final int limit = 5;
        return songRepository.findSongListByWord(word, limit);
    }
}
