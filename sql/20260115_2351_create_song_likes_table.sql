CREATE TABLE song_likes
(
    song_likes_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '음원-좋아요 고유 식별자',
    user_id       BIGINT NOT NULL COMMENT '유저 고유 식별자(FK)',
    song_id       BIGINT NOT NULL COMMENT '음원 고유 식별자(FK)',

    PRIMARY KEY (song_likes_id),

    UNIQUE KEY uk_song_likes_user_id_song_id (user_id, song_id),

    KEY           idx_song_likes_user_id (user_id),
    KEY           idx_song_likes_song_id (song_id),

    CONSTRAINT fk_song_likes_user_id
        FOREIGN KEY (user_id) REFERENCES users (user_id),

    CONSTRAINT fk_song_likes_song_id
        FOREIGN KEY (song_id) REFERENCES songs (song_id)

) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;