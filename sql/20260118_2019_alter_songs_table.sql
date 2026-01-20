ALTER TABLE songs
    ADD COLUMN jamendo_song_id BIGINT NULL COMMENT 'Jamendo 음원 고유 식별자',
    ADD UNIQUE KEY uk_songs_jamendo_song_id (jamendo_song_id);
