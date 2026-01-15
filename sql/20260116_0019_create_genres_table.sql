CREATE TABLE genres
(
    genre_id   BIGINT      NOT NULL AUTO_INCREMENT COMMENT '장르 고유 식별자',
    genre_name VARCHAR(20) NOT NULL COMMENT '장르 이름',

    PRIMARY KEY (genre_id),

    UNIQUE KEY uk_genres_genre_name (genre_name)

) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;