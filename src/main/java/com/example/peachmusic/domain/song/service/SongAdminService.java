package com.example.peachmusic.domain.song.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.enums.FileType;
import com.example.peachmusic.common.enums.JobStatus;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.CursorParam;
import com.example.peachmusic.common.model.NextCursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.storage.FileStorageService;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artistalbum.repository.ArtistAlbumRepository;
import com.example.peachmusic.domain.artistsong.entity.ArtistSong;
import com.example.peachmusic.domain.artistsong.repository.ArtistSongRepository;
import com.example.peachmusic.domain.genre.entity.Genre;
import com.example.peachmusic.domain.genre.repository.GenreRepository;
import com.example.peachmusic.domain.song.dto.request.AdminSongCreateRequestDto;
import com.example.peachmusic.domain.song.dto.request.AdminSongUpdateRequestDto;
import com.example.peachmusic.domain.song.dto.response.AdminSongAudioUpdateResponseDto;
import com.example.peachmusic.domain.song.dto.response.AdminSongCreateResponseDto;
import com.example.peachmusic.domain.song.dto.response.AdminSongUpdateResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.songgenre.entity.SongGenre;
import com.example.peachmusic.domain.songgenre.repository.SongGenreRepository;
import com.example.peachmusic.domain.streamingjob.entity.StreamingJob;
import com.example.peachmusic.domain.streamingjob.repository.StreamingJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import static com.example.peachmusic.common.constants.SearchViewSize.DETAIL_SIZE;
import static com.example.peachmusic.common.constants.UserViewScope.ADMIN_VIEW;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongAdminService {

    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;
    private final SongGenreRepository songGenreRepository;
    private final ArtistSongRepository artistSongRepository;
    private final ArtistAlbumRepository artistAlbumRepository;
    private final FileStorageService fileStorageService;
    private final StreamingJobRepository streamingJobRepository;

    /**
     * 음원 생성
     */
    @Transactional
    public AdminSongCreateResponseDto createSong(AdminSongCreateRequestDto requestDto, MultipartFile audio) {

        Album findAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(requestDto.getAlbumId())
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        if (songRepository.existsByAlbumAndName(findAlbum, requestDto.getName())) {
            throw new CustomException(ErrorCode.SONG_EXIST_NAME);
        }

        if (songRepository.existsSongByAlbumAndPosition(findAlbum, requestDto.getPosition())) {
            throw new CustomException(ErrorCode.ALBUM_EXIST_SONG_POSITION);
        }

        String storedPath = storeAudio(audio, requestDto.getName());

        try {
            Song song = new Song(findAlbum, requestDto.getName(), requestDto.getDuration(), requestDto.getLicenseCcurl(), requestDto.getPosition(), storedPath, requestDto.getVocalinstrumental(), requestDto.getLang(), requestDto.getSpeed(), requestDto.getInstruments(), requestDto.getVartags());

            Song saveSong = songRepository.save(song);

            List<Genre> genreList = genreRepository.findAllById(requestDto.getGenreIdList());

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

            StreamingJob streamingJob = new StreamingJob(song, JobStatus.READY);

            streamingJobRepository.save(streamingJob);

            return AdminSongCreateResponseDto.from(saveSong, genreNameList, findAlbum);

        } catch (RuntimeException e) {
            // DB 실패 시 업로드된 파일이 남지 않도록 정리
            cleanupFileQuietly(storedPath);
            throw e;
        }
    }

    /**
     * 음원 전체 조회
     */
    @Transactional(readOnly = true)
    public KeysetResponse<SongSearchResponseDto> getSongList(String word, CursorParam cursor) {

        final int size = DETAIL_SIZE;

        List<SongSearchResponseDto> content = songRepository.findSongKeysetPageByWord(word, size, ADMIN_VIEW, null, null, cursor);

        return KeysetResponse.of(content, size, last -> new NextCursor(last.getSongId(), null));
    }

    /**
     * 음원 기본 정보 수정
     */
    @Transactional
    public AdminSongUpdateResponseDto updateSong(AdminSongUpdateRequestDto requestDto, Long songId) {

        Song findSong = songRepository.findBySongIdAndIsDeletedFalse(songId)
                .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        Album findAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(requestDto.getAlbumId())
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        Long newPosition = requestDto.getPosition();
        if (newPosition != null && songRepository.existsSongByAlbumAndPositionAndSongIdNot(findAlbum, newPosition, songId)) {
            throw new CustomException(ErrorCode.ALBUM_EXIST_SONG_POSITION);
        }

        findSong.updateSong(requestDto, newPosition, findAlbum);

        songGenreRepository.deleteAllBySong(findSong);
        songGenreRepository.flush();

        List<Genre> findGenreList = genreRepository.findAllById(requestDto.getGenreIdList());

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
     * 음원 파일 수정
     */
    @Transactional
    public AdminSongAudioUpdateResponseDto updateAudio(Long songId, MultipartFile audio) {

        Song findSong = songRepository.findBySongIdAndIsDeletedFalse(songId)
                .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        // 기존 파일 경로 백업
        String oldPath = findSong.getAudio();

        String newPath = storeAudio(audio, findSong.getName());

        try {
            findSong.updateAudio(newPath);
        } catch (RuntimeException e) {
            // DB 반영 실패하면 새로 저장한 파일 정리
            cleanupFileQuietly(newPath);
            throw e;
        }

        if (oldPath != null && !oldPath.equals(newPath) && isManagedFilePath(oldPath)) {
            fileStorageService.deleteFileByPath(oldPath);
        }

        return AdminSongAudioUpdateResponseDto.from(findSong);
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
            throw new CustomException(ErrorCode.SONG_EXIST_ACTIVATION_SONG);
        }

        findSong.restoreSong();

    }

    // 트랜잭션 외부 파일 정리용
    private void cleanupFileQuietly(String path) {
        if (path == null || path.isBlank()) {
            return;
        }
        try {
            fileStorageService.deleteFileByPath(path);
        } catch (RuntimeException ignored) {
            log.warn("파일 정리에 실패했습니다. path={}", path, ignored);
        }
    }

    private String storeAudio(MultipartFile audio, String name) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String baseName = "PeachMusic_song_" + name + "_" + date;
        return fileStorageService.storeFile(audio, FileType.AUDIO, baseName);
    }

    private boolean isManagedFilePath(String path) {
        return !path.startsWith("https://");
    }
}
