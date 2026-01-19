package com.example.peachmusic.domain.song.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.genre.entity.Genre;
import com.example.peachmusic.domain.genre.repository.GenreRepository;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.model.request.AdminSongCreateRequestDto;
import com.example.peachmusic.domain.song.model.request.AdminSongUpdateRequestDto;
import com.example.peachmusic.domain.song.model.response.AdminSongCreateResponseDto;
import com.example.peachmusic.domain.song.model.response.AdminSongGetAllResponseDto;
import com.example.peachmusic.domain.song.model.response.AdminSongUpdateResponseDto;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.songGenre.entity.SongGenre;
import com.example.peachmusic.domain.songGenre.repository.SongGenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SongAdminService {

    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;
    private final SongGenreRepository songGenreRepository;

    /**
     * 음원 생성
     */
    @Transactional
    public AdminSongCreateResponseDto createSong(AdminSongCreateRequestDto requestDto) {

        // 1. 음원 생성할 때 소속 시켜줄 앨범 찾아오기
        Album findAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(requestDto.getAlbumId())
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        // 2. 요청 받은 음원의 데이터가 중복 되는지 검증
        if (songRepository.existsSongByAlbum_AlbumIdAndPosition(findAlbum.getAlbumId(), requestDto.getPosition())) {
            throw new CustomException(ErrorCode.ALBUM_EXIST_SONG_POSITION);
        }

        // 2. 요청 받은 데이터 담긴 음원 객체 생성
        Song song = new Song(findAlbum, requestDto.getName(), requestDto.getDuration(), requestDto.getLicenseCcurl(), requestDto.getPosition(), requestDto.getAudio(), requestDto.getVocalinstrumental(), requestDto.getLang(), requestDto.getSpeed(), requestDto.getInstruments(), requestDto.getVartags());

        // 3. 음원 레포지토리를 통해 DB에 저장
        Song saveSong = songRepository.save(song);

        // 4. 요청 받은 장르를 장르 레포지토리를 통해서 전부 찾아오고
        //    담아줄 장르 리스트에 담아주기
        List<Genre> genreList = genreRepository.findAllById(requestDto.getGenreId());

        // 5. 장르 리스트 순회하면서 음원과 각 장르로 SongGenre 객체 리스트 생성
        List<SongGenre> songGenreList = genreList.stream().map(genre -> new SongGenre(saveSong, genre)).toList();

        // 6. 중간 테이블 리스트를 중간테이블 레포지토리를 통해 DB에 전부 저장
        songGenreRepository.saveAll(songGenreList);

        // 7. 중간 테이블 리스트를 순회하면서 중간테이블->음원->음원이름을 추출해서 문자열 리스트로 변환
        List<String> genreNameList = genreList.stream().map(Genre::getGenreName).toList();

        // 8. 응답Dto from메서드 실행 후 반환
        return AdminSongCreateResponseDto.from(saveSong, genreNameList, requestDto.getAlbumId());

    }

    /**
     * 음원 전체 조회
     */
    @Transactional(readOnly = true)
    public Page<AdminSongGetAllResponseDto> getSongAll(Pageable pageable) {

        // 1. isDeleted가 false인 모든 음악을 레포지토리 통해 DB에서 음원을 담을 수 있는 페이지에 담아서 가져옴
        Page<Song> findSongPage = songRepository.findAllByIsDeletedFalse(pageable);

        // 2. 만약 가져왔는데 비어있다면 빈 데이터 반환
        if (findSongPage.isEmpty()) {
            return null;
        }

        // 3. 찾아온 음원 페이지를 변환해서 응답dto의 메서드 실행 시 반환
        return findSongPage.map(AdminSongGetAllResponseDto::from);

    }

    /**
     * 음원 정보 수정
     */
    @Transactional
    public AdminSongUpdateResponseDto updateSong(AdminSongUpdateRequestDto requestDto, Long songId) {

        // 1. 요청 받은 songId로 음원 찾아오기
        Song findSong = songRepository.findBySongIdAndIsDeletedFalse(songId)
                .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        // 2. 요청 받은 albumId로 앨범 찾아오기
        Album findAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(requestDto.getAlbumId())
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        // 3. 찾아온 음원의 updateSong 메서드 실행
        findSong.updateSong(requestDto.getName(), requestDto.getDuration(), requestDto.getLicenseCcurl(), requestDto.getPosition(), requestDto.getAudio(), requestDto.getVocalinstrumental(), requestDto.getLang(), requestDto.getSpeed(), requestDto.getInstruments(), requestDto.getVartags(), findAlbum);

        // 4. 찾아온 song에 관한 songGenre 데이터 초기화 / 즉시 동기화
        songGenreRepository.deleteAllBySong(findSong);
        songGenreRepository.flush();

        // 5. 찾아온 장르 리스트를 장르 레포지토리 통해 장르 DB에서 찾아오기
        List<Genre> findGenreList = genreRepository.findAllById(requestDto.getGenreId());

        // 6. 찾아온 장르 리스트를 순회하며 새로운 중간테이블 객체 생성 리스트로 변환
        List<SongGenre> songGenreList = findGenreList.stream().map(genre -> new SongGenre(findSong, genre)).toList();

        // 7. 중간 테이블 리스트를 중간테이블 레포지토리를 통해 DB에 전부 저장
        songGenreRepository.saveAll(songGenreList);

        // 8. 중간 테이블 리스트를 순회하면서 중간테이블->음원->음원이름을 추출해서 문자열 리스트로 변환
        List<String> genreNameList = findGenreList.stream().map(Genre::getGenreName).toList();

        // 9. 응답dto의 from메서드 실행 후 반환
        return AdminSongUpdateResponseDto.from(findSong, genreNameList, requestDto.getAlbumId());

    }

    /**
     * 음원 삭제 (비활성화)
     */
    @Transactional
    public void deleteSong(Long songId) {

        // 1. 요청 받은 songId로 음원 찾아오기
        Song findSong = songRepository.findBySongIdAndIsDeletedFalse(songId)
                .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        // 2. 요청 받은 songId가 이미 비활성화 상태인지 확인
        if (findSong.isDeleted()) {
            throw new CustomException(ErrorCode.SONG_NOT_FOUND);
        }

        // 3. 찾아온 음원의 deleteSong 메서드 실행
        findSong.deleteSong();

    }

    /**
     * 음원 복구 (활성화)
     */
    @Transactional
    public void restoreSong(Long songId) {

        // 1. 요청 받은 songId로 음원 찾아오기
        Song findSong = songRepository.findById(songId)
                .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        // 2. 요청 받은 songId가 이미 활성화 상태인지 확인
        if (!findSong.isDeleted()) {
            throw new CustomException(ErrorCode.SONG_NOT_FOUND);
        }

        // 3. 찾아온 음원의 restoreSong 메서드 실행
        findSong.restoreSong();

    }

}
