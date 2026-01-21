CREATE TABLE playlist_songs
(
    playlist_song_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '플레이리스트-음원 고유 식별자',
    song_id          BIGINT NOT NULL COMMENT '음원 고유 식별자(FK)',
    playlist_id      BIGINT NOT NULL COMMENT '플레이리스트 고유 식별자(FK)',

    PRIMARY KEY (playlist_song_id),

    UNIQUE KEY uk_playlist_song_song_id_playlist_id (song_id, playlist_id),

    KEY              idx_playlist_song_song_id (song_id),
    KEY              idx_playlist_song_playlist_id (playlist_id),

    CONSTRAINT fk_playlist_song_song_id
        FOREIGN KEY (song_id) REFERENCES songs (song_id),

    CONSTRAINT fk_playlist_song_playlist_id
        FOREIGN KEY (playlist_id) REFERENCES playlists (playlist_id)

) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;