package com.example.peachmusic.domain.openapi.jamendo.service;

import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artistAlbum.repository.ArtistAlbumRepository;
import com.example.peachmusic.domain.artistSong.entity.ArtistSong;
import com.example.peachmusic.domain.artistAlbum.entity.ArtistAlbum;
import com.example.peachmusic.domain.artistSong.repository.ArtistSongRepository;
import com.example.peachmusic.domain.genre.entity.Genre;
import com.example.peachmusic.domain.genre.repository.GenreRepository;
import com.example.peachmusic.domain.openapi.jamendo.dto.JamendoMusicInfoDto;
import com.example.peachmusic.domain.openapi.jamendo.dto.JamendoSongDto;
import com.example.peachmusic.domain.openapi.jamendo.dto.JamendoSongResponseDto;
import com.example.peachmusic.domain.openapi.jamendo.dto.JamendoTagDto;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.songGenre.entity.SongGenre;
import com.example.peachmusic.domain.songGenre.repository.SongGenreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JamendoSongService {

    private final JamendoApiService jamendoApiService;

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;
    private final ArtistSongRepository artistSongRepository;
    private final ArtistAlbumRepository artistAlbumRepository;
    private final SongGenreRepository songGenreRepository;

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

        Set<Long> existingSongIdSet = songRepository.findSongIdSet();
        List<Artist> existingArtistList = artistRepository.findArtistList();
        Map<Long, Artist> artistMap = existingArtistList.stream()
                .collect(Collectors.toMap(
                        Artist::getJamendoArtistId,
                        Function.identity()
                ));
        List<Album> existingAlbumList = albumRepository.findAlbumList();
        Map<Long, Album> albumMap = existingAlbumList.stream()
                .collect(Collectors.toMap(
                        Album::getJamendoAlbumId,
                        Function.identity()
                ));
        List<Genre> existingGenreList = genreRepository.findAll();
        Map<String, Genre> genreMap = existingGenreList.stream()
                .collect(Collectors.toMap(
                        Genre::getGenreName,
                        Function.identity()
                ));

        Set<Artist> artistSet = new HashSet<>();
        Set<Album> albumSet = new HashSet<>();
        Set<Genre> genreSet = new HashSet<>();
        Set<Song> songSet = new HashSet<>();
        Set<ArtistSong> artistSongSet = new HashSet<>();
        Set<ArtistAlbum> artistAlbumSet = new HashSet<>();
        Set<SongGenre> songGenreSet = new HashSet<>();

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

            // 페이지 단위로 음원 처리
            for (JamendoSongDto dto : results) {
                Long jamendoSongId = dto.getJamendoSongId();
                Long jamendoArtistId = dto.getArtistId();
                Long jamendoAlbumId = dto.getAlbumId();

                // 이미 저장된 음원이면 저장 안함
                if (jamendoSongId == null || existingSongIdSet.contains(jamendoSongId)) {
                    skipCount++;
                    skipInPage++;
                    continue;
                }
                existingSongIdSet.add(jamendoSongId);

                // 저장된 아티스트 또는 생성한 아티스트
                Artist artist = artistMap.computeIfAbsent(jamendoArtistId, id -> new Artist(dto.getArtistName(), jamendoArtistId));

                // 저장된 앨범 또는 생성한 앨범
                Album album = albumMap.computeIfAbsent(jamendoAlbumId, id -> new Album(jamendoAlbumId, dto.getAlbumName(), dto.getAlbumReleaseDate(), dto.getAlbumImage()));

                JamendoMusicInfoDto musicInfo = dto.getMusicInfo();
                JamendoTagDto tags = (musicInfo != null) ? musicInfo.getTags() : null;
                Song song = createSong(musicInfo, tags, dto, album);

                if (!existingArtistList.contains(artist)) {
                    artistSet.add(artist);
                }
                if (!existingAlbumList.contains(album)) {
                    albumSet.add(album);
                }

                songSet.add(song);
                artistSongSet.add(new ArtistSong(artist, song));
                artistAlbumSet.add(new ArtistAlbum(artist, album));
                addSongGenres(song, tags, genreSet, songGenreSet, genreMap);

                successCount++;
                insertedInPage++;
            }

            log.info("page={} 결과 → 신규 {}, 중복 {}", page, insertedInPage, skipInPage);

            // 마지막 페이지면 종료
            if (results.size() < limit) {
                log.info("마지막 페이지 -> 적재 종료 page : {}", page);
                break;
            }
        }

        artistRepository.saveAll(artistSet);
        albumRepository.saveAll(albumSet);
        songRepository.saveAll(songSet);
        genreRepository.saveAll(genreSet);
        artistSongRepository.saveAll(artistSongSet);
        artistAlbumRepository.saveAll(artistAlbumSet);
        songGenreRepository.saveAll(songGenreSet);

        log.info("=== Jamendo 적재 완료 ===");
        log.info("성공: {}, 중복: {}", successCount, skipCount);
    }

    /**
     * 음악 - 장르 수집
     */
    private void addSongGenres(Song song, JamendoTagDto tags, Set<Genre> genreList, Set<SongGenre> songGenreList, Map<String, Genre> genreMap) {
        // 장르 관계가 있으면 song_genres 추가
        if(tags == null || tags.getGenres() == null) {
            return;
        }

        for (String genreName : tags.getGenres()) {
            Genre genre = genreMap.computeIfAbsent(genreName, Genre::new);
            genreList.add(genre); // getOrCreateGenre(genreName);
            songGenreList.add(new SongGenre(song, genre));
        }
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
     * List<String> → 악기 / 분위기 문자열 변환
     */
    private String joinList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return String.join(",", list);
    }
}