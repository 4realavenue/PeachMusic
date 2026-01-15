CREATE TABLE album_likes
(
    album_likes_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '앨범-좋아요 고유 식별자',
    user_id        BIGINT NOT NULL COMMENT '유저 고유 식별자(FK)',
    album_id         BIGINT NOT NULL COMMENT '앨범 고유 식별자(FK)',

    PRIMARY KEY (album_likes_id),

    UNIQUE KEY uk_album_likes_user_id_album_id (user_id, album_id),

    KEY              idx_album_likes_user_id (user_id),
    KEY              idx_album_likes_album_id (album_id),

    CONSTRAINT fk_album_likes_user_id
        FOREIGN KEY (user_id) REFERENCES users (user_id),

    CONSTRAINT fk_albums_likes_album_id
        FOREIGN KEY (album_id) REFERENCES albums (album_id)

) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;