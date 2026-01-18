package com.example.peachmusic.domain.song.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
import com.example.peachmusic.common.model.PageResponse;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.genre.entity.Genre;
import com.example.peachmusic.domain.genre.repository.GenreRepository;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.model.SongDto;
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

import java.util.ArrayList;
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
     * @param requestDto
     * @return
     */
    @Transactional
    public AdminSongCreateResponseDto createSong(AdminSongCreateRequestDto requestDto) {

        // 1. 음원 생성할 때 소속 시켜줄 앨범 찾아오기
        Album findAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(requestDto.getAlbumId())
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        // 2. 요청 받은 데이터 담긴 음원 객체 생성
        Song song = new Song(findAlbum, requestDto.getName(), requestDto.getDuration(), requestDto.getLicenseCcurl(), requestDto.getPosition(), requestDto.getAudio(), requestDto.getVocalinstrumental(), requestDto.getLang(), requestDto.getSpeed(), requestDto.getInstruments(), requestDto.getVartags());

        // 3. 음원 레포지토리를 통해 DB에 저장
        Song saveSong = songRepository.save(song);

        // 4. 요청 받은 장르를 장르 레포지토리를 통해서 전부 찾아오고
        //    담아줄 장르 리스트에 담아주기
        List<Genre> genreList = genreRepository.findAllById(requestDto.getGenreId());

        // 5. 중간테이블에 저장 해줄거니까 받아줄 리스트 만들기
        List<SongGenre> songGenreList = new ArrayList<>();

        // 6. 반복문으로 장르 리스트 순회
        //    중간 테이블 저장한 음원과 장르 넣어서 객체 생성하고 중간테이블 리스트에 담아주기
        for (Genre g : genreList) {
            SongGenre songGenre = new SongGenre(saveSong, g);
            songGenreList.add(songGenre);
        }
        // 7. 중간 테이블 리스트를 중간테이블 레포지토리를 통해 DB에 전부 저장
        songGenreRepository.saveAll(songGenreList);

        // 8. 응답 반환할 때 사용할 장르 이름 담아줄 리스트 만들기
        List<String> genreNameList = new ArrayList<>();

        // 9. 반복문으로 장르 리스트 순회
        //    장르 이름 리스트에 장르의 이름 가져와서 담아주기
        for (Genre g : genreList) {
            genreNameList.add(g.getGenreName());
        }

        SongDto songDto = SongDto.from(saveSong);

        return AdminSongCreateResponseDto.from(songDto, genreNameList, requestDto.getAlbumId());

    }

    /**
     * 음원 전체 조회
     *
     * @param pageable
     * @return
     */
    @Transactional(readOnly = true)
    public PageResponse.PageData<AdminSongGetAllResponseDto> getSongAll(Pageable pageable) {

        // 1. isDeleted가 false인 모든 음악을 레포지토리 통해 DB에서 Song 담을 수 있는 Page에 담아서 가져옴
        Page<Song> findSongPage = songRepository.findAllByIsDeletedFalse(pageable);

        // 2. 만약 가져왔는데 비어있다면 빈 데이터 반환
        if (findSongPage.isEmpty()) {
            return null;
        }

        // 3. song 담은 Page를 SongDto 담은 Page로 변환
        Page<SongDto> songDtoPage = findSongPage.map(SongDto::from);

        // 4. songDto 담은 Page를 응답Dto를 담은 Page로 변환
        Page<AdminSongGetAllResponseDto> responseDtoPage = songDtoPage.map(AdminSongGetAllResponseDto::from);

        //    todo 정책 확인 필요 (Controller와 Service 레이어의 책임 분리 범위 관련)
        // 5. 응답Dto를 담은 Page를 공통 페이지 응답 객체의 내부 클래스인 핵심 데이터(PageData)로 변환(객체화) 후 반환
        return new PageResponse.PageData<>(responseDtoPage);

    }

    /**
     * 음원 정보 수정
     *
     * @param requestDto
     * @param songId
     * @return
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

        // 4. 요청 받은 song에 관한 songGenre 데이터 초기화 / 즉시 동기화
        songGenreRepository.deleteAllBySong(findSong);
        songGenreRepository.flush();

        // 5. 요청 받은 장르 리스트를 장르 레포지토리 통해 장르 DB에서 찾아오기
        List<Genre> findGenreList = genreRepository.findAllById(requestDto.getGenreId());

        // 6. 중간테이블에 저장 해줄거니까 받아줄 리스트 만들기
        List<SongGenre> songGenreList = new ArrayList<>();

        // 7. 반복문으로 장르 리스트 순회
        //    중간 테이블 저장한 음원과 장르 넣어서 객체 생성하고 중간테이블 리스트에 담아주기
        for (Genre g : findGenreList) {
            SongGenre songGenre = new SongGenre(findSong, g);
            songGenreList.add(songGenre);
        }
        // 8. 중간 테이블 리스트를 중간테이블 레포지토리를 통해 DB에 전부 저장
        songGenreRepository.saveAll(songGenreList);

        // 9. 응답 반환할 때 사용할 장르 이름 담아줄 리스트 만들기
        List<String> genreNameList = new ArrayList<>();

        // 10. 반복문으로 장르 리스트 순회
        //    장르 이름 리스트에 장르의 이름 가져와서 담아주기
        for (Genre g : findGenreList) {
            genreNameList.add(g.getGenreName());
        }

        SongDto songDto = SongDto.from(findSong);

        return AdminSongUpdateResponseDto.from(songDto, genreNameList, requestDto.getAlbumId());

    }

    /**
     * 음원 삭제 (비활성화)
     *
     * @param songId
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
     * @param songId
     */
    @Transactional
    public void restore(Long songId) {

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
