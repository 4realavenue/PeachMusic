CREATE TABLE artist_likes
(
    artist_likes_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '아티스트-좋아요 고유 식별자',
    user_id         BIGINT NOT NULL COMMENT '유저 고유 식별자(FK)',
    artist_id       BIGINT NOT NULL COMMENT '아티스트 고유 식별자(FK)',

    PRIMARY KEY (artist_likes_id),

    UNIQUE KEY uk_artist_albums_user_id_artist_id (user_id, artist_id),

    KEY             idx_artist_likes_user_id (user_id),
    KEY             idx_artist_likes_artist_id (artist_id),

    CONSTRAINT fk_artists_likes_user_id
        FOREIGN KEY (user_id) REFERENCES users (user_id),

    CONSTRAINT fk_artists_likes_artist_id
        FOREIGN KEY (artist_id) REFERENCES artists (artist_id)

) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;