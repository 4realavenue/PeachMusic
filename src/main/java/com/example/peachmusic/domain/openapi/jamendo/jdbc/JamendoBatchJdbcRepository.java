package com.example.peachmusic.domain.openapi.jamendo.jdbc;

import com.example.peachmusic.domain.openapi.jamendo.jdbc.row.ArtistAlbumRow;
import com.example.peachmusic.domain.openapi.jamendo.jdbc.row.ArtistSongRow;
import com.example.peachmusic.domain.openapi.jamendo.jdbc.row.SongGenreRow;
import com.example.peachmusic.domain.song.entity.Song;
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

    public void insertSongs(List<Song> songs) {
        if (songs.isEmpty()) return;

        String sql = """
            INSERT IGNORE INTO songs
            (jamendo_song_id, album_id, name, duration, license_ccurl,
             position, audio, vocalinstrumental, lang, speed,
             instruments, vartags, like_count)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
        """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Song s = songs.get(i);
                ps.setLong(1, s.getJamendoSongId());
                ps.setLong(2, s.getAlbum().getAlbumId());
                ps.setString(3, s.getName());
                ps.setLong(4, s.getDuration());
                ps.setString(5, s.getLicenseCcurl());
                ps.setLong(6, s.getPosition());
                ps.setString(7, s.getAudio());
                ps.setString(8, s.getVocalinstrumental());
                ps.setString(9, s.getLang());
                ps.setString(10, s.getSpeed());
                ps.setString(11, s.getInstruments());
                ps.setString(12, s.getVartags());
            }

            @Override
            public int getBatchSize() {
                return songs.size();
            }
        });
    }

    public void insertArtistSongs(List<ArtistSongRow> rows) {
        if (rows.isEmpty()) return;

        String sql = """
            INSERT IGNORE INTO artist_songs (artist_id, song_id)
            VALUES (?, ?)
        """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ArtistSongRow r = rows.get(i);
                ps.setLong(1, r.artistId());
                ps.setLong(2, r.songId());
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }

    public void insertArtistAlbums(List<ArtistAlbumRow> rows) {
        if (rows.isEmpty()) return;

        String sql = """
            INSERT IGNORE INTO artist_albums (artist_id, album_id)
            VALUES (?, ?)
        """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ArtistAlbumRow r = rows.get(i);
                ps.setLong(1, r.artistId());
                ps.setLong(2, r.albumId());
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }

    public void insertSongGenres(List<SongGenreRow> rows) {
        if (rows.isEmpty()) return;

        String sql = """
            INSERT IGNORE INTO song_genres (song_id, genre_id)
            VALUES (?, ?)
        """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SongGenreRow r = rows.get(i);
                ps.setLong(1, r.songId());
                ps.setLong(2, r.genreId());
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }
}
