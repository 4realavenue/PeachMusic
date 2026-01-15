CREATE TABLE song_genres
(
    song_genre_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '음원-장르 고유 식별자',
    song_id       BIGINT NOT NULL COMMENT '음원 고유 식별자(FK)',
    genre_id      BIGINT NOT NULL COMMENT '장르 고유 식별자(FK)',

    PRIMARY KEY (song_genre_id),

    UNIQUE KEY uk_song_genre_song_id_genre_id (song_id, genre_id),

    KEY           idx_song_genre_song_id (song_id),
    KEY           idx_song_genre_genre_id (genre_id),

    CONSTRAINT fk_song_genre_song_id
        FOREIGN KEY (song_id) REFERENCES songs (song_id),

    CONSTRAINT fk_song_genre_genre_id
        FOREIGN KEY (genre_id) REFERENCES genres (genre_id)

) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;