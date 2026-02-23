ALTER TABLE search_histories
    CHANGE COLUMN search_id history_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '검색어 고유 식별자';