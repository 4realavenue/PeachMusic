package com.example.peachmusic.domain.song.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artistAlbum.repository.ArtistAlbumRepository;
import com.example.peachmusic.domain.artistSong.entity.ArtistSong;
import com.example.peachmusic.domain.artistSong.repository.ArtistSongRepository;
import com.example.peachmusic.domain.genre.entity.Genre;
import com.example.peachmusic.domain.genre.repository.GenreRepository;
import com.example.peachmusic.domain.song.dto.request.AdminSongCreateRequestDto;
import com.example.peachmusic.domain.song.dto.request.AdminSongUpdateRequestDto;
import com.example.peachmusic.domain.song.dto.response.AdminSongCreateResponseDto;
import com.example.peachmusic.domain.song.dto.response.AdminSongUpdateResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponse;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.songGenre.entity.SongGenre;
import com.example.peachmusic.domain.songGenre.repository.SongGenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.peachmusic.common.enums.UserRole.ADMIN;

@Service
@RequiredArgsConstructor
public class SongAdminService {

    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;
    private final SongGenreRepository songGenreRepository;
    private final ArtistSongRepository artistSongRepository;
    private final ArtistAlbumRepository artistAlbumRepository;
    private final ArtistRepository artistRepository;

    /**
     * 음원 생성
     */
    @Transactional
    public AdminSongCreateResponseDto createSong(AdminSongCreateRequestDto requestDto) {

        Album findAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(requestDto.getAlbumId())
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        if (songRepository.existsSongByAudio(requestDto.getAudio())) {
            throw new CustomException(ErrorCode.SONG_EXIST_SONG_URL);
        }

        if (songRepository.existsSongByAlbumAndPosition(findAlbum, requestDto.getPosition())) {
            throw new CustomException(ErrorCode.ALBUM_EXIST_SONG_POSITION);
        }

        Song song = new Song(findAlbum, requestDto.getName(), requestDto.getDuration(), requestDto.getLicenseCcurl(), requestDto.getPosition(), requestDto.getAudio(), requestDto.getVocalinstrumental(), requestDto.getLang(), requestDto.getSpeed(), requestDto.getInstruments(), requestDto.getVartags());

        Song saveSong = songRepository.save(song);

        List<Genre> genreList = genreRepository.findAllById(requestDto.getGenreId());

        List<SongGenre> songGenreList = genreList.stream()
                .map(genre -> new SongGenre(saveSong, genre))
                .toList();

        songGenreRepository.saveAll(songGenreList);

        List<Artist> findArtistList = artistAlbumRepository.findArtist_ArtistIdByArtistAlbum_Album_AlbumId(findAlbum.getAlbumId());

        List<ArtistSong> artistSongList = findArtistList.stream()
                .map(artistSong -> new ArtistSong(artistSong, saveSong))
                .toList();

        artistSongRepository.saveAll(artistSongList);

        List<String> genreNameList = genreList.stream()
                .map(Genre::getGenreName)
                .toList();

        return AdminSongCreateResponseDto.from(saveSong, genreNameList, findAlbum);

    }

    /**
     * 음원 전체 조회
     */
    @Transactional(readOnly = true)
    public Page<SongSearchResponse> getSongAll(String word, Pageable pageable) {
        return songRepository.findSongPageByWord(word, pageable, ADMIN);
    }

    /**
     * 음원 정보 수정
     */
    @Transactional
    public AdminSongUpdateResponseDto updateSong(AdminSongUpdateRequestDto requestDto, Long songId) {

        Song findSong = songRepository.findBySongIdAndIsDeletedFalse(songId)
                .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        Album findAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(requestDto.getAlbumId())
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        String newAudio = requestDto.getAudio();
        if (songRepository.existsByAudioAndSongIdNot(newAudio, songId)) {
            throw new CustomException(ErrorCode.SONG_EXIST_SONG_URL);
        }

        Long newPosition = requestDto.getPosition();
        if (songRepository.existsSongByAlbumAndPosition(findAlbum, requestDto.getPosition())) {
            throw new CustomException(ErrorCode.ALBUM_EXIST_SONG_POSITION);
        }

        findSong.updateSong(requestDto, newAudio, newPosition, findAlbum);

        songGenreRepository.deleteAllBySong(findSong);
        songGenreRepository.flush();

        List<Genre> findGenreList = genreRepository.findAllById(requestDto.getGenreId());

        List<SongGenre> songGenreList = findGenreList.stream()
                .map(genre -> new SongGenre(findSong, genre))
                .toList();

        songGenreRepository.saveAll(songGenreList);

        List<String> genreNameList = findGenreList.stream()
                .map(Genre::getGenreName)
                .toList();

        return AdminSongUpdateResponseDto.from(findSong, genreNameList, findAlbum);

    }

    /**
     * 음원 삭제 (비활성화)
     */
    @Transactional
    public void deleteSong(Long songId) {

        Song findSong = songRepository.findBySongIdAndIsDeletedFalse(songId)
                .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        findSong.deleteSong();

    }

    /**
     * 음원 복구 (활성화)
     */
    @Transactional
    public void restoreSong(Long songId) {

        Song findSong = songRepository.findById(songId)
                .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        if (!findSong.isDeleted()) {
            throw new CustomException(ErrorCode.SONG_ALREADY_ACTIVE);
        }

        findSong.restoreSong();

    }

}
