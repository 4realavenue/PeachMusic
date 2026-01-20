ALTER TABLE search_words
    ADD COLUMN search_date DATE NOT NULL COMMENT '검색 날짜',
    DROP INDEX search_words_word;