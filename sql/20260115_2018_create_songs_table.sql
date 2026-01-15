CREATE TABLE songs
(
    song_id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '음원 고유 식별자',
    album_id          BIGINT       NOT NULL COMMENT '앨범 고유 식별자 (FK)',
    name              VARCHAR(255) NOT NULL COMMENT '음원 이름',
    duration          BIGINT       NOT NULL COMMENT '음원 길이',
    license_ccrurl    VARCHAR(255) NULL COMMENT '저작권 정보',
    position          BIGINT       NOT NULL COMMENT '수록 순서',
    audio             VARCHAR(255) NOT NULL COMMENT '음원 url',
    vocalinstrumental VARCHAR(255) NULL COMMENT '음원 보컬 유무',
    lang              VARCHAR(255) NULL COMMENT '음원 언어',
    speed             VARCHAR(255) NULL COMMENT '음원 속도',
    instruments       VARCHAR(255) NULL COMMENT '악기 종류',
    vartags           VARCHAR(255) NULL COMMENT '음원 분위기',
    like_count        BIGINT       NOT NULL COMMENT '좋아요 수',
    is_deleted        TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '회원 삭제 여부, 0: 삭제 안됨 / 1: 삭제',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시점',
    modified_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시점',

    PRIMARY KEY (song_id),

    UNIQUE KEY uk_songs_audio (audio),

    KEY idx_songs_album_id (album_id),
    CONSTRAINT fk_songs_album
        FOREIGN KEY (album_id)
            REFERENCES albums (album_id)

) CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;