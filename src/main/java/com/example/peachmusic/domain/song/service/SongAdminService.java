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
import com.example.peachmusic.domain.song.model.request.SongCreateRequestDto;
import com.example.peachmusic.domain.song.model.response.AdminSongCreateResponseDto;
import com.example.peachmusic.domain.song.model.response.AdminSongGetAllResponseDto;
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
    public AdminSongCreateResponseDto createSong(SongCreateRequestDto requestDto) {

        // 1. 음원 생성할 때 소속 시켜줄 앨범 찾아오기
        //    todo findByIdAndIsDeletedFalse 로 변경 예정
        Album findAlbum = albumRepository.findById(requestDto.getAlbumId())
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        // 2. 요청 받은 데이터 담긴 음원 객체 생성
        Song song = new Song(findAlbum, requestDto.getName(), requestDto.getDuration(), requestDto.getLicenseCcurl(), requestDto.getPosition(), requestDto.getAudio(), requestDto.getVocalinstrumental(), requestDto.getLang(), requestDto.getSpeed(), requestDto.getInstruments(), requestDto.getVartags());

        // 3. 음원 레포지토리를 통해 DB에 저장
        Song saveSong = songRepository.save(song);

        // 4. 요청 받은 장르를 장르 레포지토리를 통해서 전부 찾아오고
        //    담아줄 장르 리스트에 담아주기
        List<Genre> genreList = genreRepository.findAllById(requestDto.getGenreId());

        //    todo Song-Genre 서비스에서 수행해줄 로직 (5~7)
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

        return AdminSongCreateResponseDto.from(songDto, genreNameList, findAlbum.getAlbumId());
    }

    /**
     * 음원 전체 조회
     * @param pageable
     * @return
     */
    @Transactional(readOnly = true)
    public PageResponse.PageData<AdminSongGetAllResponseDto> getSongAll(Pageable pageable) {

        // 1. isDeleted가 false인 모든 음악을 레포지토리 통해 DB에서 Song 담을 수 있는 Page에 담아서 가져옴
        Page<Song> findSongPage = songRepository.findAllByIsDeletedFalse(pageable);

        // 2. 만약 가져왔는데 비어있다면 예외 던져주기
        //    todo 정책 확인 필요 (비어 있는 성공 응답 or 확실한 예외 처리)
        if (findSongPage.isEmpty()) {
            throw new CustomException(ErrorCode.SONG_NOT_FOUND);
        }

        // 3. song 담은 Page를 SongDto 담은 Page로 변환
        Page<SongDto> songDtoPage = findSongPage.map(SongDto::from);

        // 4. songDto 담은 Page를 응답Dto를 담은 Page로 변환
        Page<AdminSongGetAllResponseDto> responseDtoPage = songDtoPage.map(AdminSongGetAllResponseDto::from);

        //    todo 정책 확인 필요 (Controller와 Service 레이어의 책임 분리 범위 관련)
        // 5. 응답Dto를 담은 Page를 공통 페이지 응답 객체의 내부 클래스인 핵심 데이터(PageData)로 변환(객체화) 후 반환
        return new PageResponse.PageData<>(responseDtoPage);
    }


}
