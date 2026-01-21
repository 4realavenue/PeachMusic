package com.example.peachmusic.domain.openApi.jamendo.service;

import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artistAlbum.entity.ArtistAlbum;
import com.example.peachmusic.domain.artistAlbum.repository.ArtistAlbumRepository;
import com.example.peachmusic.domain.artistSong.entity.ArtistSong;
import com.example.peachmusic.domain.artistSong.repository.ArtistSongRepository;
import com.example.peachmusic.domain.genre.entity.Genre;
import com.example.peachmusic.domain.genre.repository.GenreRepository;
import com.example.peachmusic.domain.openApi.jamendo.dto.JamendoMusicInfoDto;
import com.example.peachmusic.domain.openApi.jamendo.dto.JamendoSongDto;
import com.example.peachmusic.domain.openApi.jamendo.dto.JamendoSongResponseDto;
import com.example.peachmusic.domain.openApi.jamendo.dto.JamendoTagDto;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.songGenre.entity.SongGenre;
import com.example.peachmusic.domain.songGenre.repository.SongGenreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JamendoSongService {

    private final JamendoApiService jamendoApiService;

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;

    private final SongGenreRepository songGenreRepository;
    private final ArtistSongRepository artistSongRepository;
    private final ArtistAlbumRepository artistAlbumRepository;

    /**
     * Jamendo 음원 초기 적재
     * - 페이지 단위로 api 호출
     * - 이미 존재하는 음원은 제외
     * - 데이터가 없으면 생성
     */
    @Transactional
    public void importInitJamendo(LocalDate startDate, LocalDate endDate) {
        int maxPage = 200;
        String dateBetween = startDate + "_" + endDate;
        duplicationJamendo(maxPage, dateBetween);
    }

    /**
     * Jamendo 음원 매일 3시 정기 적재
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void importScheduledJamendo() {
        int maxPage = 50;
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate yesterday = today.minusDays(1);
        String dateBetween = yesterday + "_" + today;
        duplicationJamendo(maxPage, dateBetween);
    }

    /**
     * Jamendo 중복 코드
     */
    public void duplicationJamendo(int maxPage, String dateBetween) {
        int successCount = 0;
        int skipCount = 0;
        int limit = 200;

        for (int page = 1; page <= maxPage; page++) {

            log.info("Jamendo 적재 중... page={}", page);

            // api 호출 결과 -> response안에 result가 들어있음.
            JamendoSongResponseDto response = jamendoApiService.fetchSongs(page, limit, dateBetween);

            // 이번 페이지에서 내려온 음원 목록 // 시간을 찍어보자
            List<JamendoSongDto> results = response.getResults();

            // 더 이상 데이터 없으면 적재 종료
            if (results == null || results.isEmpty()) {
                log.info("Api 응답 결과 : 데이터 없음. 적재 종료");
                break;
            }

            int insertedInPage = 0;
            int skipInPage = 0;

            // 페이지 안의 음원들을 하나씩 반복
            for (JamendoSongDto dto : results) {

                // 음원 중복 체크
                if (songRepository.existsByJamendoSongId(dto.getJamendoSongId())) {
                    skipCount++; // 이미 있는 음원이면 skip
                    skipInPage++;
                    continue;
                }

                // 아티스트 조회 및 생성
                Artist artist = getOrCreateArtist(dto.getArtistName());
                // 앨범 조회 및 생성
                Album album = getOrCreateAlbum(dto);

                // musicInfo가 없는 경우도 존재 musicInfo가 없으면 tags도 없음.
                JamendoMusicInfoDto musicInfo = dto.getMusicInfo();
                // musicInfo가 있을 때만 악기, 분위기, 장르를 사용
                JamendoTagDto tags = (musicInfo != null) ? musicInfo.getTags() : null;

                String instruments = null;
                String vartags = null;

                // tags가 있을때 악기/분위기 ,로 묶기
                if (tags != null) {
                    instruments = joinList(tags.getInstruments());
                    vartags = joinList(tags.getMoods());
                }

                // 음원 저장
                Song song = new Song(dto.getJamendoSongId(), album, dto.getName(),
                        dto.getDuration() != null ? dto.getDuration().longValue() : 0L, dto.getLicenseCcurl(),
                        dto.getPosition() != null ? dto.getPosition().longValue() : 0L, dto.getAudioUrl(),
                        musicInfo != null ? musicInfo.getVocalInstrumental() : null,
                        musicInfo != null ? musicInfo.getLang() : null,
                        musicInfo != null ? musicInfo.getSpeed() : null, instruments, vartags
                );

                // 음원 저장
                songRepository.save(song);

                // 아티스트_음원 중간 테이블 저장(중복 방지)
                existsArtistAndSong(artist, song);
                // 아티스트_앨범 중간 테이블 저장(중복 방지)
                existsArtistAndAlbum(artist, album);

                if (tags != null && tags.getGenres() != null) {
                    for (String genreName : tags.getGenres()) {

                        // 장르 조회 및 생성
                        Genre genre = getOrCreateGenre(genreName);

                        // 음원_장르 중간 테이블 저장
                        if (!songGenreRepository.existsBySong_SongIdAndGenre_GenreId(song.getSongId(), genre.getGenreId())) {
                            songGenreRepository.save(new SongGenre(song, genre));
                        }
                    }
                }
                successCount++;
                insertedInPage++;
            }

            log.info("page={} 결과 → 신규 {}, 중복 {}", page, insertedInPage, skipInPage);

            if (results.size() < limit) {
                log.info("마지막 페이지 -> 적재 종료 page : {}", page);
                break;
            }
        }

        log.info("=== Jamendo 적재 완료 ===");
        log.info("성공: {}, 중복: {}", successCount, skipCount);
    }

    /**
     * 장르 조회 및 생성
     */
    private Genre getOrCreateGenre(String genreName) {
        Genre genre = genreRepository.findByGenreName(genreName);

        if (genre == null) {
            genre = new Genre(genreName);
            genreRepository.save(genre);
        }
        return genre;
    }

    /**
     * 아티스트 조회 및 생성
     */
    private Artist getOrCreateArtist(String artistName) {
        return artistRepository.findByArtistName(artistName)
                .orElseGet(() -> {
                        Artist artist = new Artist(artistName);
                        return artistRepository.save(artist);
                });
    }

    /**
     * 앨범 조회 및 생성
     */
    private Album getOrCreateAlbum(JamendoSongDto dto) {
        return albumRepository.findByJamendoAlbumId(dto.getAlbumId())
                .orElseGet(() -> {
                        Album album = new Album(dto.getAlbumId(), dto.getAlbumName(), dto.getAlbumReleaseDate(), dto.getAlbumImage());
                        return albumRepository.save(album);
                });
    }

    /**
     * 아티스트_음원 중간 테이블 매핑
     */
    private void existsArtistAndSong(Artist artist, Song song) {
        if (!artistSongRepository.existsByArtist_ArtistIdAndSong_SongId(artist.getArtistId(), song.getSongId())) {
            artistSongRepository.save(new ArtistSong(artist, song)
            );
        }
    }

    /**
     * 아티스트_앨범 중간 테이블  매핑
     */
    private void existsArtistAndAlbum(Artist artist, Album album) {
        if (!artistAlbumRepository.existsByArtist_ArtistIdAndAlbum_AlbumId(artist.getArtistId(), album.getAlbumId())) {
            artistAlbumRepository.save(new ArtistAlbum(artist, album));
        }
    }

    /**
     * List<String> → 악기랑 분위기는 ,로 문자열
     */
    private String joinList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return String.join(",", list);
    }
}