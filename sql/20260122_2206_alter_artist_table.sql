ALTER TABLE artists
    ADD COLUMN jamendo_artist_id BIGINT NULL COMMENT 'Jamendo 아티스트 고유 식별자',
    ADD UNIQUE KEY uk_songs_jamendo_artist_id (jamendo_artist_id);
