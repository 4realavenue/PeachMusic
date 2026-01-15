CREATE TABLE playlists
(
    playlist_id   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '플레이리스트 고유 식별자',
    user_id       BIGINT       NOT NULL COMMENT '유저 고유 식별자',
    playlist_name VARCHAR(255) NOT NULL COMMENT '플레이리스트 이름',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시점',
    modified_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시점',

    PRIMARY KEY (playlist_id),

    KEY           idx_playlist_user_id (user_id),

    CONSTRAINT fk_playlist_user_id
        FOREIGN KEY (user_id) REFERENCES users (user_id)

) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;