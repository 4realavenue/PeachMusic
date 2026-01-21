CREATE TABLE artists
(
    artist_id   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '아티스트 고유 식별자',
    artist_name VARCHAR(255) NOT NULL COMMENT '아티스트 이름',
    like_count  BIGINT       NOT NULL COMMENT '좋아요 수',
    is_deleted  TINYINT(1) DEFAULT 0 COMMENT '아티스트 삭제 여부, 0: 삭제 안됨 / 1: 삭제',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시점',
    modified_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시점',

    PRIMARY KEY (artist_id)

) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;