package com.example.peachmusic.domain.openapi.jamendo.service;

import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.genre.entity.Genre;
import com.example.peachmusic.domain.genre.repository.GenreRepository;
import com.example.peachmusic.domain.openapi.jamendo.dto.JamendoMusicInfoDto;
import com.example.peachmusic.domain.openapi.jamendo.dto.JamendoSongDto;
import com.example.peachmusic.domain.openapi.jamendo.dto.JamendoSongResponseDto;
import com.example.peachmusic.domain.openapi.jamendo.dto.JamendoTagDto;
import com.example.peachmusic.domain.openapi.jamendo.jdbc.JamendoBatchJdbcRepository;
import com.example.peachmusic.domain.openapi.jamendo.jdbc.row.ArtistAlbumRow;
import com.example.peachmusic.domain.openapi.jamendo.jdbc.row.ArtistSongRow;
import com.example.peachmusic.domain.openapi.jamendo.jdbc.row.SongGenreRow;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class JamendoSongService {

    private final JamendoApiService jamendoApiService;
    private final JamendoBatchJdbcRepository batchJdbcRepository;

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;


    /**
     * Jamendo 음원 초기 적재
     */
    @Transactional
    public void importInitJamendo(LocalDate startDate, LocalDate endDate) {
        int maxPage = 50;
        String dateBetween = startDate + "_" + endDate;
        duplicationJamendo(maxPage, dateBetween);
    }

    /**
     * Jamendo 음원 매일 3시 정기 적재
     */
    @Transactional
    public void importScheduledJamendo() {
        int maxPage = 50;
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate yesterday = today.minusDays(1);
        String dateBetween = yesterday + "_" + today;
        duplicationJamendo(maxPage, dateBetween);
    }

    /**
     * Jamendo 적재 핵심 로직
     */
    public void duplicationJamendo(int maxPage, String dateBetween) {

        int limit = 200;
        int successCount = 0;
        int skipCount = 0;

        for (int page = 1; page <= maxPage; page++) {

            log.info("Jamendo 적재 중... page={}", page);

            // Jamendo API 호출
            JamendoSongResponseDto response =
                    jamendoApiService.fetchSongs(page, limit, dateBetween);

            // 이번 페이지에서 내려온 음원 목록
            List<JamendoSongDto> results = response.getResults();

            // 내려온 데이터가 없으면 종료
            if (results == null || results.isEmpty()) {
                log.info("Api 응답 결과 : 데이터 없음. 적재 종료");
                break;
            }

            int insertedInPage = 0;
            int skipInPage = 0;

            // 이번 페이지에 내려온 jamendo_song_id 전부 수집
            Set<Long> pageSongIds = new HashSet<>(results.size() * 2);
            for (JamendoSongDto dto : results) {
                Long songId = dto.getJamendoSongId();
                if (songId != null) {
                    pageSongIds.add(songId);
                }
            }

            // DB에 이미 존재하는 jamendo_song_id 한 번에 조회
            Set<Long> existingSongIds = songRepository.findExistingJamendoSongIds(pageSongIds);

            List<Song> songs = new ArrayList<>();
            List<ArtistSongRow> artistSongRows = new ArrayList<>();
            List<ArtistAlbumRow> artistAlbumRows = new ArrayList<>();
            List<SongGenreRow> songGenreRows = new ArrayList<>();

            // 페이지 단위로 음원 처리
            for (JamendoSongDto dto : results) {

                Long jamendoSongId = dto.getJamendoSongId();

                // 이미 존재하는 음원이면 skip
                if (jamendoSongId == null || existingSongIds.contains(jamendoSongId)) {
                    skipCount++;
                    skipInPage++;
                    continue;
                }

                // 아티스트 조회 또는 생성
                Artist artist = getOrCreateArtist(dto.getArtistName());

                // 앨범 조회 또는 생성
                Album album = getOrCreateAlbum(dto);

                // musicInfo가 없는 경우도 존재
                JamendoMusicInfoDto musicInfo = dto.getMusicInfo();
                JamendoTagDto tags = (musicInfo != null) ? musicInfo.getTags() : null;

                String instruments = null;
                String vartags = null;

                // tags가 있을 때만 악기 / 분위기 문자열 생성
                if (tags != null) {
                    instruments = joinList(tags.getInstruments());
                    vartags = joinList(tags.getMoods());
                }

                // 음원 엔티티 생성 (JPA save X, insert 용)
                Song song = new Song(jamendoSongId, album, dto.getName(),
                        dto.getDuration() != null ? dto.getDuration() : 0L, dto.getLicenseCcurl(),
                        dto.getPosition() != null ? dto.getPosition() : 0L, dto.getAudioUrl(),
                        musicInfo != null ? musicInfo.getVocalInstrumental() : null,
                        musicInfo != null ? musicInfo.getLang() : null,
                        musicInfo != null ? musicInfo.getSpeed() : null,
                        instruments, vartags
                );

                // 음원 batch insert 대상에 추가
                // 실제 DB insert는 페이지 처리 끝난 뒤 한 번에 수행됨
                songs.add(song);
                // 아티스트-음원 관계 추가
                artistSongRows.add(new ArtistSongRow(artist.getArtistId(), jamendoSongId));
                // 아티스트-앨범 관계 추가
                artistAlbumRows.add(new ArtistAlbumRow(artist.getArtistId(), album.getAlbumId()));

                // 장르 관계가 있으면 song_genres 추가
                if (tags != null && tags.getGenres() != null) {
                    for (String genreName : tags.getGenres()) {
                        Genre genre = getOrCreateGenre(genreName);
                        songGenreRows.add(new SongGenreRow(jamendoSongId, genre.getGenreId()));
                    }
                }

                successCount++;
                insertedInPage++;
            }

            // 이번 페이지에서 수집한 데이터들을 JDBC insert로 한 번에 저장
            batchJdbcRepository.insertSongs(songs);
            batchJdbcRepository.insertArtistSongs(artistSongRows);
            batchJdbcRepository.insertArtistAlbums(artistAlbumRows);
            batchJdbcRepository.insertSongGenres(songGenreRows);

            log.info("page={} 결과 → 신규 {}, 중복 {}", page, insertedInPage, skipInPage);

            // 마지막 페이지면 종료
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
        return genreRepository.findByGenreName(genreName)
                .orElseGet(() -> genreRepository.save(new Genre(genreName)));
    }

    /**
     * 아티스트 조회 및 생성
     */
    private Artist getOrCreateArtist(String artistName) {
        return artistRepository.findByArtistName(artistName)
                .orElseGet(() -> artistRepository.save(new Artist(artistName)));
    }

    /**
     * 앨범 조회 및 생성
     */
    private Album getOrCreateAlbum(JamendoSongDto dto) {
        return albumRepository.findByJamendoAlbumId(dto.getAlbumId())
                .orElseGet(() -> albumRepository.save(new Album(dto.getAlbumId(), dto.getAlbumName(), dto.getAlbumReleaseDate(), dto.getAlbumImage())));
    }

    /**
     * List<String> → 악기 / 분위기 문자열 변환
     */
    private String joinList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return String.join(",", list);
    }
}