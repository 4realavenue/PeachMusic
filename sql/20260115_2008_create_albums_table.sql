CREATE TABLE albums
(
    album_id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '앨범 고유 식별자',
    album_name         VARCHAR(255) NOT NULL COMMENT '앨범 이름',
    album_release_date DATE         NOT NULL COMMENT '앨범 발매일',
    album_image        VARCHAR(255) NOT NULL COMMENT '앨범 이미지',
    like_count         BIGINT       NOT NULL COMMENT '좋아요 수',
    is_deleted         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '앨범 삭제 여부, 0: 삭제 안됨 / 1: 삭제',
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시점',
    modified_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시점',

    PRIMARY KEY (album_id),

    UNIQUE KEY uk_albums_album_image (album_image)

) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;