package com.example.peachmusic.domain.openapi.jamendo.jdbc;

import com.example.peachmusic.domain.openapi.jamendo.jdbc.row.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class JamendoBatchJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void upsertSongs(List<SongRow> songs) {
        if (songs.isEmpty()) {
            return;
        }

        String insertSQL = """
                    INSERT INTO songs (
                        jamendo_song_id, album_id, name, duration, license_ccurl,
                        position, audio, vocalinstrumental, lang, speed,
                        instruments, vartags, like_count
                    )
                    SELECT
                        ?, a.album_id, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0
                    FROM albums a
                    WHERE a.jamendo_album_id = ?
                    ON DUPLICATE KEY UPDATE
                        name = VALUES(name),
                        duration = VALUES(duration),
                        license_ccurl = VALUES(license_ccurl),
                        position = VALUES(position),
                        audio = VALUES(audio),
                        vocalinstrumental = VALUES(vocalinstrumental),
                        lang = VALUES(lang),
                        speed = VALUES(speed),
                        instruments = VALUES(instruments),
                        vartags = VALUES(vartags),
                        modified_at = CURRENT_TIMESTAMP
                """;

        jdbcTemplate.batchUpdate(insertSQL, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SongRow s = songs.get(i);
                ps.setLong(1, s.jamendoSongId());
                ps.setString(2, s.name());
                ps.setLong(3, s.duration());
                ps.setString(4, s.licenseCcurl());
                ps.setLong(5, s.position());
                ps.setString(6, s.audio());
                ps.setString(7, s.vocalInstrumental());
                ps.setString(8, s.lang());
                ps.setString(9, s.speed());
                ps.setString(10, s.instruments());
                ps.setString(11, s.vartags());
                ps.setLong(12, s.jamendoAlbumId());
            }

            @Override
            public int getBatchSize() {
                return songs.size();
            }
        });
    }


    public void insertArtistSongs(List<ArtistSongRow> rows) {
        if (rows.isEmpty()) {
            return;
        }

        String insertSQL = """
                    INSERT IGNORE INTO artist_songs (artist_id, song_id)
                    SELECT
                        ar.artist_id,
                        s.song_id
                    FROM artists ar
                    JOIN songs s
                    WHERE ar.jamendo_artist_id = ?
                      AND s.jamendo_song_id = ?
                """;

        jdbcTemplate.batchUpdate(insertSQL, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ArtistSongRow r = rows.get(i);
                ps.setLong(1, r.jamendoArtistId());
                ps.setLong(2, r.jamendoSongId());
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }

    public void insertArtistAlbums(List<ArtistAlbumRow> rows) {
        if (rows.isEmpty()) {
            return;
        }

        String insertSQL = """
                    INSERT IGNORE INTO artist_albums (artist_id, album_id)
                    SELECT
                        ar.artist_id,
                        al.album_id
                    FROM artists ar
                    JOIN albums al
                    WHERE ar.jamendo_artist_id = ?
                      AND al.jamendo_album_id = ?
                """;

        jdbcTemplate.batchUpdate(insertSQL, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ArtistAlbumRow r = rows.get(i);
                ps.setLong(1, r.jamendoArtistId());
                ps.setLong(2, r.jamendoAlbumId());
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }

    public void insertSongGenres(List<SongGenreRow> rows) {
        if (rows.isEmpty()) {
            return;
        }

        String insertSQL = """
                    INSERT IGNORE INTO song_genres (song_id, genre_id)
                    SELECT
                        s.song_id, g.genre_id
                    FROM songs s
                    JOIN genres g ON g.genre_name = ?
                    WHERE s.jamendo_song_id = ?
                """;

        jdbcTemplate.batchUpdate(insertSQL, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SongGenreRow r = rows.get(i);
                ps.setString(1, r.genreName());
                ps.setLong(2, r.jamendoSongId());
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }

    public void upsertArtists(List<ArtistRow> rows) {
        if (rows.isEmpty()) {
            return;
        }

        String insertSQL = """
                    INSERT INTO artists
                    (artist_name, jamendo_artist_id, like_count)
                    VALUES (?, ?, 0)
                    ON DUPLICATE KEY UPDATE
                        artist_name = VALUES(artist_name),
                        modified_at = CURRENT_TIMESTAMP
                """;

        jdbcTemplate.batchUpdate(insertSQL, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ArtistRow r = rows.get(i);
                ps.setString(1, r.artistName());
                ps.setLong(2, r.jamendoArtistId());
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }

    public void upsertAlbums(List<AlbumRow> rows) {
        if (rows.isEmpty()) {
            return;
        }

        String insertSQL = """
                    INSERT INTO albums
                    (album_name, album_release_date, album_image, jamendo_album_id, like_count)
                    VALUES (?, ?, ?, ?, 0)
                    ON DUPLICATE KEY UPDATE
                        album_name = VALUES(album_name),
                        album_release_date = VALUES(album_release_date),
                        album_image = VALUES(album_image),
                        modified_at = CURRENT_TIMESTAMP
                """;

        jdbcTemplate.batchUpdate(insertSQL, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                AlbumRow r = rows.get(i);
                ps.setString(1, r.albumName());
                ps.setObject(2, r.albumReleaseDate()); // LocalDate
                ps.setString(3, r.albumImage());
                ps.setLong(4, r.jamendoAlbumId());
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }

    public void upsertGenres(List<GenreRow> rows) {
        if (rows.isEmpty()) {
            return;
        }

        String insertSQL = """
                    INSERT INTO genres (genre_name)
                    VALUES (?)
                    ON DUPLICATE KEY UPDATE
                        genre_name = VALUES(genre_name)
                """;

        jdbcTemplate.batchUpdate(insertSQL, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                GenreRow r = rows.get(i);
                ps.setString(1, r.genreName());
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }

    public void upsertStreamingJobs(List<StreamingJobRow> rows) {
        if (rows.isEmpty()) {
            return;
        }

        String insertSQL = """
                    INSERT INTO streaming_jobs (song_id, job_status)
                    SELECT
                    s.song_id,
                    ?
                    FROM songs s
                    WHERE s.jamendo_song_id = ?
                    ON DUPLICATE KEY UPDATE
                        job_status = VALUES(job_status)
                """;

        jdbcTemplate.batchUpdate(insertSQL, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                StreamingJobRow r = rows.get(i);
                ps.setString(1, r.jobStatus());
                ps.setLong(2, r.jamendoSongId());
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }
}