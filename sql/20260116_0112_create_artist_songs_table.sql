CREATE TABLE artist_songs
(
    artist_song_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '아티스트-음원 고유 식별자',
    artist_id      BIGINT NOT NULL COMMENT '아티스트 고유 식별자(FK)',
    song_id        BIGINT NOT NULL COMMENT '음원 고유 식별자(FK)',

    PRIMARY KEY (artist_song_id),

    UNIQUE KEY uk_artist_songs_artist_id_song_id (artist_id, song_id),

    KEY            idx_artist_song_artist_id (artist_id),
    KEY            idx_artist_song_song_id (song_id),

    CONSTRAINT fk_artist_song_artist_id
        FOREIGN KEY (artist_id) REFERENCES artists (artist_id),

    CONSTRAINT fk_artist_song_song_id
        FOREIGN KEY (song_id) REFERENCES songs (song_id)

) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;