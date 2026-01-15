CREATE TABLE search_words
(
    search_word_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '검색어 고유 식별자',
    word VARCHAR(100) NOT NULL COMMENT '검색어',
    count BIGINT NOT NULL COMMENT '검색 횟수',

    PRIMARY KEY (search_word_id),

    UNIQUE KEY search_words_word (word)

) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;