CREATE TABLE artist_albums
(
    artist_albums_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '아티스트-앨범 고유 식별자',
    artist_id        BIGINT NOT NULL COMMENT '아티스트 고유 식별자(FK)',
    album_id         BIGINT NOT NULL COMMENT '앨범 고유 식별자(FK)',

    PRIMARY KEY (artist_albums_id),

    UNIQUE KEY uk_artist_albums_artist_id_album_id (artist_id, album_id),

    KEY              idx_artist_albums_artist_id (artist_id),
    KEY              idx_artist_albums_album_id (album_id),

    CONSTRAINT fk_artist_albums_artist_id
        FOREIGN KEY (artist_id) REFERENCES artists (artist_id),

    CONSTRAINT fk_artist_albums_album_id
        FOREIGN KEY (album_id) REFERENCES albums (album_id)

) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;