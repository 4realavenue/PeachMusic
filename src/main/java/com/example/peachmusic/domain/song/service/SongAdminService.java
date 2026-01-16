package com.example.peachmusic.domain.song.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.genre.entity.Genre;
import com.example.peachmusic.domain.genre.repository.GenreRepository;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.model.SongDto;
import com.example.peachmusic.domain.song.model.request.SongCreateRequestDto;
import com.example.peachmusic.domain.song.model.response.SongCreateResponseDto;
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
public class SongAdminService {

    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;
    private final SongGenreRepository songGenreRepository;

    @Transactional
    public SongCreateResponseDto createSong(SongCreateRequestDto requestDto) {

        // 1.
        Album findAlbum = albumRepository.findById(requestDto.getAlbumId())
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        // 2.
        Song song = new Song(findAlbum, requestDto.getName(), requestDto.getDuration(), requestDto.getLicenseCcurl(), requestDto.getPosition(), requestDto.getAudio(), requestDto.getVocalinstrumental(), requestDto.getLang(), requestDto.getSpeed(), requestDto.getInstruments(), requestDto.getVartags());

        // 3.
        Song saveSong = songRepository.save(song);

        // 4. 요청 받은 아이디 리스트 중에 장르 레포지토리에 존재하는 데이터 갖고 와서 리스트에 담아줘
        List<Genre> genreList = genreRepository.findAllById(requestDto.getGenreId());

        // todo Song-Genre 서비스에서 수행해줄 로직 (5~7)
        // 5. 중간테이블에 저장할거니까 받아줄 리스트를 만들어
        List<SongGenre> songGenreList = new ArrayList<>();

        // 6.
        for (Genre g : genreList) {
            SongGenre songGenre = new SongGenre(song, g);
            songGenreList.add(songGenre);
        }
        // 7.
        songGenreRepository.saveAll(songGenreList);


        // 8.
        List<String> genreNameList = new ArrayList<>();

        for (Genre g : genreList) {
            genreNameList.add(g.getGenreName());
        }


        SongDto songDto = SongDto.from(saveSong);

        return SongCreateResponseDto.from(songDto, genreNameList, findAlbum.getAlbumId());

    }


}
