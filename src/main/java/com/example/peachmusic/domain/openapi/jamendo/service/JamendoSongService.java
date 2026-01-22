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
        int limit = 200;        // Jamendo API 한 페이지당 조회 개수
        int successCount = 0;   // 전체 신규 적재된 음원 수
        int skipCount = 0;      // 전체 중복(스킵) 음원 수

        // Jamendo API 페이지 단위로 반복
        for (int page = 1; page <= maxPage; page++) {
            int insertedInPage = 0; // 현재 페이지에서 신규 적재된 음원 수
            int skipInPage = 0;     // 현재 페이지에서 중복으로 스킵된 음원 수

            log.info("Jamendo 적재 중... page={}", page);

            JamendoSongResponseDto response = jamendoApiService.fetchSongs(page, limit, dateBetween);
            List<JamendoSongDto> results = response.getResults();

            if (results == null || results.isEmpty()) {
                log.info("Api 응답 결과 : 데이터 없음. 적재 종료");
                break;
            }

            Set<Long> pageSongList = collectPageSongIdList(results);
            Set<Long> existingSongIds = songRepository.findJamendoSongIdList(pageSongList);

            List<Song> songs = new ArrayList<>();
            List<ArtistSongRow> artistSongRows = new ArrayList<>();
            List<ArtistAlbumRow> artistAlbumRows = new ArrayList<>();
            List<SongGenreRow> songGenreRows = new ArrayList<>();

            // 페이지 단위로 음원 처리
            for (JamendoSongDto dto : results) {

                Long jamendoSongId = dto.getJamendoSongId();

                if (jamendoSongId == null || existingSongIds.contains(jamendoSongId)) {
                    skipCount++;
                    skipInPage++;
                    continue;
                }

                Artist artist = getOrCreateArtist(dto.getArtistName());
                Album album = getOrCreateAlbum(dto);
                JamendoMusicInfoDto musicInfo = dto.getMusicInfo();
                JamendoTagDto tags = (musicInfo != null) ? musicInfo.getTags() : null;
                Song song = createSong(musicInfo, tags, dto, album);
                
                songs.add(song);
                artistSongRows.add(new ArtistSongRow(artist.getArtistId(), jamendoSongId));
                artistAlbumRows.add(new ArtistAlbumRow(artist.getArtistId(), album.getAlbumId()));
                addSongGenres(jamendoSongId, tags, songGenreRows);

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
     * 음악 - 장르 수집
     */
    private void addSongGenres(Long jamendoSongId, JamendoTagDto tags, List<SongGenreRow> songGenreRows) {
        // 장르 관계가 있으면 song_genres 추가
        if(tags == null || tags.getGenres() == null) {
            return;
        }
        for (String genreName : tags.getGenres()) {
            Genre genre = getOrCreateGenre(genreName);
            songGenreRows.add(new SongGenreRow(jamendoSongId, genre.getGenreId()));
        }
    }

    /**
     * 페이지 내의 jamendo_song_id 수집 메서드
     */
    private Set<Long> collectPageSongIdList(List<JamendoSongDto> results) {
        Set<Long> songIdList = new HashSet<>(results.size() * 2);
        for(JamendoSongDto dto : results) {
            if(dto.getJamendoSongId() != null) {
                songIdList.add(dto.getJamendoSongId());
            }
        }
        return songIdList;
    }

    /**
     * 음악 엔티티 생성 전용 메서드(DTO -> Song 변환 담당)
     */
    private Song createSong(JamendoMusicInfoDto musicInfo, JamendoTagDto tags, JamendoSongDto dto, Album album) {
        String instruments = (tags != null) ? joinList(tags.getInstruments()) : null;
        String vartags = (tags != null) ? joinList(tags.getMoods()) : null;

        // 음원 엔티티 생성 (JPA save X, insert 용)
        return new Song(dto.getJamendoSongId(), album, dto.getName(),
                dto.getDuration() != null ? dto.getDuration() : 0L, dto.getLicenseCcurl(),
                dto.getPosition() != null ? dto.getPosition() : 0L, dto.getAudioUrl(),
                musicInfo != null ? musicInfo.getVocalInstrumental() : null,
                musicInfo != null ? musicInfo.getLang() : null,
                musicInfo != null ? musicInfo.getSpeed() : null,
                instruments, vartags
        );
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