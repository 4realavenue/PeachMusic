package com.example.peachmusic.domain.song.service.recommend;

import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.song.dto.response.SongRecommendationResponseDto;
import com.example.peachmusic.domain.song.service.RecommendationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class RecommendationScaleTest {

    static final int DATA_SIZE = 100_000;
    static final int K = 50;
    static final int GENRE_COUNT = 100;
    @Autowired
    RecommendationService recommendationService;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    @DisplayName("Case 1: 단일 취향 추천 정밀도 테스트")
    void evaluateSingleGenre() {
        // 1~5번 유저가 각각 하나의 명확한 장르만 선호하는 경우
        Map<Long, List<Long>> singlePreference = Map.of(1L, List.of(1L), 2L, List.of(25L), 3L, List.of(50L), 4L, List.of(75L), 5L, List.of(99L));
        runTest("SINGLE", singlePreference);
    }

    @Test
    @Order(2)
    @DisplayName("Case 2: 복합 취향 추천 정밀도 테스트")
    void evaluateMultiGenre() {
        // 유저별로 2~3개의 장르가 섞여 있는 경우
        Map<Long, List<Long>> multiPreference = Map.of(1L, List.of(1L, 10L), 2L, List.of(25L, 30L, 35L), 3L, List.of(50L, 55L), 4L, List.of(75L, 80L), 5L, List.of(99L, 5L));
        runTest("MULTI", multiPreference);
    }

    private void runTest(String label, Map<Long, List<Long>> preferenceMap) {
        setupDataset(preferenceMap);

        double pSum = 0;
        int cnt = 0;

        for (Long uid : preferenceMap.keySet()) {
            AuthUser user = new AuthUser(uid, "", UserRole.USER, 0L);
            List<Long> rec = recommendationService.getRecommendedSongList(user).stream().map(SongRecommendationResponseDto::getSongId).limit(K).toList();

            Set<Long> truth = new HashSet<>(jdbcTemplate.queryForList("SELECT song_id FROM song_likes WHERE user_id=?", Long.class, uid));

            if (truth.isEmpty()) continue;
            long hit = rec.stream().filter(truth::contains).count();
            double p = (double) hit / K;
            pSum += p;
            cnt++;
            System.out.printf("[%s] User%d → Hit:%d P=%.4f\n", label, uid, hit, p);
        }

        System.out.println("\n==============================");
        System.out.println(label + " AVERAGE P@" + K + ": " + (pSum / cnt));
        System.out.println("==============================\n");
    }

    // Setup & Insert Helpers
    private void setupDataset(Map<Long, List<Long>> preferenceMap) {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        List<String> tables = List.of("song_likes", "playlist_songs", "playlists", "artist_songs", "song_genres", "songs", "albums", "artists", "users", "genres");
        for (String t : tables) {
            try {
                jdbcTemplate.execute("TRUNCATE TABLE " + t);
            } catch (Exception ignore) {
            }
        }
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        insertUsers();
        insertGenres();
        insertArtist();
        insertAlbum();
        insertSongs();
        insertSongGenres();
        insertArtistSongs();
        insertPlaylists();

        // 인자로 받은 취향 맵에 따라 좋아요 데이터 생성
        for (Long uid : preferenceMap.keySet()) {
            List<Long> gids = preferenceMap.get(uid);
            for (Long gid : gids) {
                jdbcTemplate.update("""
                            INSERT INTO song_likes (user_id, song_id)
                            SELECT ?, sg.song_id FROM song_genres sg WHERE sg.genre_id = ? ORDER BY RAND() LIMIT ? 
                        """, uid, gid, 300 / gids.size());
            }
        }
    }

    private void insertUsers() {
        for (int i = 1; i <= 5; i++) {
            jdbcTemplate.update("""
                        INSERT INTO users (user_id, name, nickname, email, password, role, is_deleted, token_version, email_verified)
                        VALUES (?, ?, ?, ?, ?, 'USER', 0, '0', 1)
                    """, i, "u" + i, "u" + i, "u" + i + "@test.com", "p");
        }
    }

    private void insertGenres() {
        for (int i = 1; i <= GENRE_COUNT; i++) {
            jdbcTemplate.update("INSERT INTO genres (genre_id, genre_name) VALUES (?, ?)", i, "GENRE_" + i);
        }
    }

    private void insertArtist() {
        List<String> cols = new ArrayList<>();
        List<Object> vals = new ArrayList<>();
        addIfColumnExists("artists", "artist_id", cols, vals, 1L);
        addIfColumnExists("artists", "artist_name", cols, vals, "Artist");
        addIfColumnExists("artists", "like_count", cols, vals, 0L);
        addIfColumnExists("artists", "is_deleted", cols, vals, 0);
        dynamicInsert("artists", cols, vals);
    }

    private void insertAlbum() {
        List<String> cols = new ArrayList<>();
        List<Object> vals = new ArrayList<>();
        addIfColumnExists("albums", "album_id", cols, vals, 1L);
        addIfColumnExists("albums", "album_name", cols, vals, "Album");
        addIfColumnExists("albums", "album_release_date", cols, vals, java.sql.Date.valueOf(java.time.LocalDate.now()));
        addIfColumnExists("albums", "album_image", cols, vals, "img");
        addIfColumnExists("albums", "like_count", cols, vals, 0L);
        addIfColumnExists("albums", "is_deleted", cols, vals, 0);
        dynamicInsert("albums", cols, vals);
    }

    private void insertSongs() {
        String[] speeds = {"high", "mid", "low"};
        String[] tags = {"energetic", "calm", "dark", "bright"};
        String[] inst = {"piano", "guitar", "drum", "violin"};

        String sql = """
                    INSERT INTO songs (song_id, album_id, name, duration, position, audio, speed, vartags, instruments, like_count, is_deleted, play_count, streaming_status)
                    VALUES (?, 1, ?, 200, 1, ?, ?, ?, ?, ?, 0, 0, 1)
                """;

        for (int i = 1; i <= DATA_SIZE; i += 1000) {
            int start = i;
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int j) throws SQLException {
                    int id = start + j;
                    int gid = (id % GENRE_COUNT) + 1;
                    ps.setLong(1, id);
                    ps.setString(2, "Song " + id);
                    ps.setString(3, "url_" + id);
                    ps.setString(4, speeds[gid % 3]);
                    ps.setString(5, tags[gid % 4]);
                    ps.setString(6, inst[gid % 4]);
                    ps.setLong(7, id % 1000);
                }

                @Override
                public int getBatchSize() {
                    return 1000;
                }
            });
        }
    }

    private void insertSongGenres() {
        jdbcTemplate.execute("INSERT INTO song_genres (song_id, genre_id) SELECT song_id, (song_id % 100) + 1 FROM songs");
    }

    private void insertArtistSongs() {
        jdbcTemplate.execute("INSERT INTO artist_songs (song_id, artist_id) SELECT song_id, 1 FROM songs");
    }

    private void insertPlaylists() {
        for (long i = 1; i <= 5; i++) {
            List<String> cols = new ArrayList<>();
            List<Object> vals = new ArrayList<>();
            addIfColumnExists("playlists", "playlist_id", cols, vals, i);
            addIfColumnExists("playlists", "user_id", cols, vals, i);
            if (columnExists("playlists", "playlist_name")) {
                cols.add("playlist_name");
                vals.add("PL" + i);
            } else if (columnExists("playlists", "name")) {
                cols.add("name");
                vals.add("PL" + i);
            }
            dynamicInsert("playlists", cols, vals);
        }
    }

    // Utilities
    private void dynamicInsert(String table, List<String> cols, List<Object> vals) {
        String colPart = String.join(", ", cols);
        String qPart = String.join(", ", Collections.nCopies(cols.size(), "?"));
        jdbcTemplate.update("INSERT INTO " + table + " (" + colPart + ") VALUES (" + qPart + ")", vals.toArray());
    }

    private void addIfColumnExists(String table, String col, List<String> cols, List<Object> vals, Object value) {
        if (columnExists(table, col)) {
            cols.add(col);
            vals.add(value);
        }
    }

    private boolean columnExists(String table, String col) {
        Integer cnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = UPPER(?) AND COLUMN_NAME = UPPER(?)", Integer.class, table, col);
        return cnt != null && cnt > 0;
    }
}