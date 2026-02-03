package com.example.peachmusic.domain.openapi.jamendo.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.enums.JobStatus;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.domain.openapi.jamendo.dto.*;
import com.example.peachmusic.domain.openapi.jamendo.jdbc.JamendoBatchJdbcRepository;
import com.example.peachmusic.domain.openapi.jamendo.jdbc.row.*;
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

    private static final int INIT_MAX_PAGE = 10000;
    private static final int DAILY_MAX_PAGE = 1000;
    private static final int MONTHLY_MAX_PAGE = 2000;

    // 앨범 ID 없는 음원 스킵 로그 샘플 5개
    private static final int ORPHAN_LOG_LIMIT = 5;

    /**
     * Jamendo 음원 초기 적재
     */
    @Transactional
    public void importInitJamendo(JamendoInitRequestDto request) {
        if(request.getStartDate().isAfter(request.getEndDate())) {
            throw new CustomException(ErrorCode.JAMENDO_INVALID_DATE_RANGE);
        }
        String dateBetween = request.getStartDate() + "_" + request.getEndDate();
        importJamendoByDateRange(INIT_MAX_PAGE, dateBetween);
    }

    /**
     * Jamendo 음원 매일 3시 정기 적재(일간 동기화)
     */
    @Transactional
    public void importScheduledJamendo() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate yesterday = today.minusDays(1);
        String dateBetween = yesterday + "_" + today;
        importJamendoByDateRange(DAILY_MAX_PAGE, dateBetween);
    }

    /**
     * Jamendo 월 1회 정합성 동기화 (최근 6개월)
     */
    @Transactional
    public void importJamendoMonthlySync() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate oneMonthAgo = today.minusMonths(6);
        String dateBetween = oneMonthAgo + "_" + today;
        importJamendoByDateRange(MONTHLY_MAX_PAGE, dateBetween);
    }

    /**
     * Jamendo 적재 핵심 로직
     */
    public void importJamendoByDateRange(int maxPage, String dateBetween) {
        int limit = 200;        // Jamendo API 한 페이지당 조회 개수
        int successCount = 0;   // 처리된 음원 수
        int orphanSkipCount = 0;// 앨범 ID 없는 orphan 음원 스킵 수
        int orphanLogCount = 0; // orphan 로그 샘플 카운트

        // Jamendo API 페이지 단위로 반복
        for (int page = 1; page <= maxPage; page++) {
            JamendoSongResponseDto response = jamendoApiService.fetchSongs(page, limit, dateBetween);
            List<JamendoSongDto> results = response.getResults();

            if (results == null || results.isEmpty()) {
                break;
            }

            List<ArtistRow> artistRowList = new ArrayList<>(); // 부모 테이블
            List<AlbumRow> albumRowList = new ArrayList<>();
            List<GenreRow> genreRowList = new ArrayList<>();
            List<SongRow> songList = new ArrayList<>();

            List<ArtistSongRow> artistSongRowList = new ArrayList<>();
            List<ArtistAlbumRow> artistAlbumRowList = new ArrayList<>();
            List<SongGenreRow> songGenreRowList = new ArrayList<>();

            // 페이지 단위로 음원 처리
            for (JamendoSongDto dto : results) {

                Long jamendoSongId = dto.getJamendoSongId();

                // 앨범 정보가 없는 orphan 음원은 FK 정합성 보장을 위해서 적재에서 제외
                if (jamendoSongId == null || dto.getJamendoAlbumId() == null) {
                    orphanSkipCount++;
                    // 중복 스킵 샘플 로그
                    if (orphanLogCount < ORPHAN_LOG_LIMIT) {
                        log.warn(
                                "[Jamendo Skip] orphan song(no album) - songId={}, albumId={}, artistId={}",
                                dto.getJamendoSongId(),
                                dto.getJamendoAlbumId(),
                                dto.getJamendoArtistId()
                        );
                        orphanLogCount++;
                    }
                    continue;
                }

                Long jamendoArtistId = dto.getJamendoArtistId();
                Long jamendoAlbumId = dto.getJamendoAlbumId();

                artistRowList.add(new ArtistRow(jamendoArtistId, dto.getJamendoArtistName()));
                albumRowList.add(new AlbumRow(jamendoAlbumId, dto.getJamendoAlbumName(), dto.getJamendoAlbumReleaseDate(), dto.getJamendoAlbumImage()));

                JamendoMusicInfoDto musicInfo = dto.getJamendoMusicInfo();
                JamendoTagDto tags = (musicInfo != null) ? musicInfo.getTags() : null;

                if (musicInfo == null || musicInfo.getSpeed() == null || tags == null || tags.getInstruments() == null || tags.getMoods() == null) {
                    continue;
                }

                songList.add(toSongRowList(dto, musicInfo, tags));
                artistSongRowList.add(new ArtistSongRow(jamendoArtistId, jamendoSongId));
                artistAlbumRowList.add(new ArtistAlbumRow(jamendoArtistId, jamendoAlbumId));

                if (tags.getGenres() != null) {
                    for (String genre : tags.getGenres()) {
                        if (genre == null || genre.isBlank()) continue;
                        genreRowList.add(new GenreRow(genre));
                        songGenreRowList.add(new SongGenreRow(jamendoSongId, genre));
                    }
                }
                successCount++;
            }

            List<StreamingJobRow> streamingJobRows = songList.stream()
                    .map(song -> new StreamingJobRow(
                            song.jamendoSongId(),
                            JobStatus.NOT_READY.name()
                    ))
                    .toList();

            // 외부 API 기준 데이터 -> 변경 가능해서 upsert(ON DUPLICATE KEY UPDATE) 사용
            batchJdbcRepository.upsertArtists(artistRowList);
            batchJdbcRepository.upsertAlbums(albumRowList);
            batchJdbcRepository.upsertGenres(genreRowList);
            batchJdbcRepository.upsertSongs(songList);
            batchJdbcRepository.upsertStreamingJobs(streamingJobRows);

            // 관계는 상태가 아니라 존재여부여서 insert ignore
            batchJdbcRepository.insertArtistSongs(artistSongRowList);
            batchJdbcRepository.insertArtistAlbums(artistAlbumRowList);
            batchJdbcRepository.insertSongGenres(songGenreRowList);

            // 마지막 페이지면 종료
            if (results.size() < limit) {
                break;
            }
        }
        log.info("=== Jamendo 적재 완료 ===");
        log.info("성공: {}, orphan 스킵(앨범 없음): {}", successCount, orphanSkipCount);
    }

    /**
     * 음악 엔티티 생성 전용 메서드(DTO -> Song 변환 담당)
     */
    private SongRow toSongRowList(JamendoSongDto dto, JamendoMusicInfoDto info, JamendoTagDto tags) {
        return new SongRow(
                dto.getJamendoSongId(),
                dto.getJamendoAlbumId(),
                dto.getJamendoSongName(),
                dto.getJamendoDuration() != null ? dto.getJamendoDuration() : 0L,
                dto.getJamendoLicenseCcurl(),
                dto.getJamendoPosition() != null ? dto.getJamendoPosition() : 0L,
                dto.getJamendoAudioUrl(),
                info != null ? info.getVocalInstrumental() : null,
                info != null ? info.getLang() : null,
                info != null ? info.getSpeed() : null,
                tags != null ? joinList(tags.getInstruments()) : null,
                tags != null ? joinList(tags.getMoods()) : null
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