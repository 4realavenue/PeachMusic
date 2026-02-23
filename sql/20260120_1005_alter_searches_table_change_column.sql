ALTER TABLE searches
    CHANGE COLUMN search_word_id search_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '검색어 고유 식별자';